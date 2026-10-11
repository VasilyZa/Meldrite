package dev.meldrite.distribution.mc1201;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/** Unpacks the locked distribution and starts the unmodified Fabric server launcher. */
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
        List<Path> artifacts = new ArrayList<>();
        int count = Integer.parseInt(required(bundle, "artifactCount"));
        for (int index = 0; index < count; index++) {
            Path target = directory.resolve(required(bundle, index + ".path")).normalize();
            if (!target.startsWith(directory.resolve("libraries/meldrite"))) {
                throw new IOException("Invalid bundled artifact path: " + target);
            }
            extract(required(bundle, index + ".resource"), target, required(bundle, index + ".sha256"));
            artifacts.add(target);
        }
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

    private static void extract(String resource, Path target, String sha256) throws Exception {
        if (!sha256.matches("[0-9a-f]{64}")) throw new IOException("Invalid bundled artifact checksum");
        if (Files.isRegularFile(target) && checksum(target).equals(sha256)) return;
        Files.createDirectories(target.getParent());
        Path temporary = Files.createTempFile(target.getParent(), "extract-", ".tmp");
        try {
            try (InputStream input = resource(resource)) {
                Files.copy(input, temporary, StandardCopyOption.REPLACE_EXISTING);
            }
            if (!checksum(temporary).equals(sha256)) {
                throw new IOException("Bundled artifact checksum mismatch: " + resource);
            }
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException unsupported) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static String checksum(Path path) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream input = new DigestInputStream(Files.newInputStream(path), digest)) {
            input.transferTo(OutputStream.nullOutputStream());
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private static InputStream resource(String name) throws IOException {
        InputStream input = ServerLauncher.class.getClassLoader().getResourceAsStream(name);
        if (input == null) throw new IOException("Missing bundled resource: " + name);
        return input;
    }

    private static String required(Properties properties, String key) throws IOException {
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
