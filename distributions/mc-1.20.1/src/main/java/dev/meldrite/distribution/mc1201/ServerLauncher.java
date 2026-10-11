package dev.meldrite.distribution.mc1201;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
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

        System.setProperty("fabric.gameJarPath", artifacts.get(Integer.parseInt(required(bundle, "minecraft"))).toString());
        String mods = joinPaths(select(bundle, "mods", artifacts));
        String additionalMods = System.getProperty("fabric.addMods", "");
        if (!additionalMods.isBlank()) mods += File.pathSeparator + additionalMods;
        System.setProperty("fabric.addMods", mods);
        var classpath = select(bundle, "classpath", artifacts);
        RuntimeClasspath.install(classpath);
        // Fabric's discovery reads this property as well as the actual class loader.
        System.setProperty("java.class.path", joinPaths(classpath));
        Thread.currentThread().setContextClassLoader(ClassLoader.getSystemClassLoader());
        List<String> arguments = new ArrayList<>(Arrays.asList(args));
        if (Arrays.stream(args).noneMatch(arg -> arg.equals("nogui") || arg.equals("--nogui"))) {
            arguments.add("nogui");
        }
        System.out.println("Starting Meldrite " + required(bundle, "meldriteVersion")
                + " with Fabric Loader " + required(bundle, "loaderVersion"));
        try {
            Class.forName(required(bundle, "mainClass"), true, ClassLoader.getSystemClassLoader())
                    .getMethod("main", String[].class).invoke(null, (Object) arguments.toArray(String[]::new));
        } catch (InvocationTargetException failure) {
            var cause = failure.getCause();
            if (cause instanceof Exception exception) throw exception;
            if (cause instanceof Error error) throw error;
            throw new RuntimeException("Fabric launcher failed", cause);
        }
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
