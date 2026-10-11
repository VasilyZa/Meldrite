package dev.meldrite.distribution.mc1201;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/** Installs locked dependencies and starts the unmodified Fabric server launcher. */
public final class ServerLauncher {
    private ServerLauncher() {}

    public static void main(String[] args) throws Exception {
        Properties bundle = new Properties();
        try (InputStream input = resource("META-INF/meldrite/bundle.properties")) {
            bundle.load(input);
        }
        int requiredJava = Integer.parseInt(required(bundle, "javaFeature"));
        if (Runtime.version().feature() < requiredJava) {
            throw new IllegalStateException("Meldrite requires Java " + requiredJava + " or newer");
        }
        Path directory = Path.of("").toAbsolutePath().normalize();
        List<Path> artifacts = DependencyInstaller.install(bundle, directory);
        Files.createDirectories(directory.resolve("mods"));
        Files.createDirectories(directory.resolve("plugins"));

        Path java = Path.of(System.getProperty("java.home"), "bin", "java");
        if (!Files.isRegularFile(java)) java = java.resolveSibling("java.exe");
        List<String> command = new ArrayList<>();
        command.add(java.toString());
        command.addAll(ManagementFactory.getRuntimeMXBean().getInputArguments());
        command.add("-Dfabric.gameJarPath=" + artifacts.get(Integer.parseInt(required(bundle, "minecraft"))));
        String mods = joinPaths(select(bundle, "mods", artifacts));
        String additionalMods = System.getProperty("fabric.addMods", "");
        if (!additionalMods.isBlank()) mods += File.pathSeparator + additionalMods;
        command.add("-Dfabric.addMods=" + mods);
        command.add("-cp");
        command.add(joinPaths(select(bundle, "classpath", artifacts)));
        command.add(required(bundle, "mainClass"));
        command.addAll(Arrays.asList(args));
        if (Arrays.stream(args).noneMatch(arg -> arg.equals("nogui") || arg.equals("--nogui"))) {
            command.add("nogui");
        }
        System.out.println("Starting Meldrite " + required(bundle, "meldriteVersion")
                + " with Fabric Loader " + required(bundle, "loaderVersion"));
        Process server = new ProcessBuilder(command).directory(directory.toFile()).inheritIO().start();
        Thread shutdown = new Thread(() -> {
            if (server.isAlive()) {
                server.destroy();
                try {
                    server.waitFor();
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "meldrite-launcher-shutdown");
        Runtime.getRuntime().addShutdownHook(shutdown);
        int exit;
        try {
            exit = server.waitFor();
        } catch (InterruptedException interrupted) {
            server.destroy();
            Thread.currentThread().interrupt();
            throw interrupted;
        }
        Runtime.getRuntime().removeShutdownHook(shutdown);
        System.exit(exit);
    }

    private static InputStream resource(String name) throws IOException {
        InputStream input = ServerLauncher.class.getClassLoader().getResourceAsStream(name);
        if (input == null) throw new IOException("Missing bundled resource: " + name);
        return input;
    }

    static String required(Properties properties, String key) throws IOException {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) throw new IOException("Missing bundled setting: " + key);
        return value;
    }

    private static List<Path> select(Properties bundle, String key, List<Path> artifacts) throws IOException {
        List<Path> result = new ArrayList<>();
        for (String index : required(bundle, key).split(",")) result.add(artifacts.get(Integer.parseInt(index)));
        return result;
    }

    private static String joinPaths(List<Path> paths) {
        return paths.stream().map(Path::toString).collect(Collectors.joining(File.pathSeparator));
    }
}
