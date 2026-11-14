package dev.meldrite.distribution.mc1201;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.jar.JarFile;

/** Installs verified libraries into the ordinary system loader before Fabric starts. */
public final class RuntimeClasspath {
    private static Instrumentation instrumentation;
    // The system loader continues using these files after the launcher main returns.
    private static final List<JarFile> libraries = new ArrayList<>();
    private RuntimeClasspath() { }

    /** The executable JAR's Launcher-Agent-Class invokes this before application main. */
    public static synchronized void agentmain(String arguments, Instrumentation value) {
        instrumentation = value;
        try {
            var server = java.lang.management.ManagementFactory.getPlatformMBeanServer();
            var name = new javax.management.ObjectName("dev.meldrite:type=RuntimeAgent");
            if (!server.isRegistered(name)) server.registerMBean(new javax.management.StandardMBean(
                    new AgentBridge(), AgentBridgeMBean.class), name);
        } catch (javax.management.JMException failure) {
            throw new IllegalStateException("Cannot publish the local runtime instrumentation bridge", failure);
        }
    }
    public interface AgentBridgeMBean { Instrumentation getInstrumentation(); }
    public static final class AgentBridge implements AgentBridgeMBean {
        @Override public Instrumentation getInstrumentation() { return instrumentation; }
    }

    static List<Path> install(List<Path> paths) throws IOException {
        if (instrumentation == null)
            throw new IllegalStateException("Start the Meldrite distribution with java -jar to initialize its runtime classpath");
        List<Path> installed = new ArrayList<>();
        for (Path path : paths) {
            path = nativePath(path.toAbsolutePath().normalize());
            var jar = new JarFile(path.toFile());
            try {
                instrumentation.appendToSystemClassLoaderSearch(jar);
                libraries.add(jar);
                installed.add(path);
            } catch (RuntimeException failure) {
                jar.close();
                throw failure;
            }
        }
        return List.copyOf(installed);
    }

    private static boolean nativeSafe(Path path) {
        String name = path.toString();
        return name.length() < 240 && name.chars().allMatch(character -> character < 128);
    }

    private static Path nativePath(Path source) throws IOException {
        // Windows JVMTI uses a native path API with stricter length/encoding limits
        // than NIO and JarFile. Keep the system loader and Fabric on the same bytes.
        if (java.io.File.separatorChar != '\\' || nativeSafe(source)) return source;
        String digest = sha256(source);
        Path directory = Path.of(System.getProperty("java.io.tmpdir"), "meldrite-runtime").toAbsolutePath().normalize();
        Path target = directory.resolve(digest + ".jar");
        if (!nativeSafe(target)) throw new IOException("RUNTIME_CLASSPATH_PATH_UNSUPPORTED: use a short ASCII directory for -Djava.io.tmpdir: " + directory);
        Files.createDirectories(directory);
        if (Files.isRegularFile(target) && sha256(target).equals(digest)) return target;
        Path temporary = Files.createTempFile(directory, "install-", ".tmp");
        try {
            Files.copy(source, temporary, StandardCopyOption.REPLACE_EXISTING);
            if (!sha256(temporary).equals(digest)) throw new IOException("RUNTIME_CLASSPATH_COPY_MISMATCH: " + source);
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException unsupported) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return target;
        } finally { Files.deleteIfExists(temporary); }
    }

    private static String sha256(Path path) throws IOException {
        final MessageDigest digest;
        try { digest = MessageDigest.getInstance("SHA-256"); }
        catch (NoSuchAlgorithmException impossible) { throw new AssertionError(impossible); }
        try (var input = Files.newInputStream(path)) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) != -1) digest.update(buffer, 0, count);
        }
        return HexFormat.of().formatHex(digest.digest());
    }
}
