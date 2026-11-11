package dev.meldrite.plugin.runtime;

import dev.meldrite.plugin.spi.DescriptorReader;
import dev.meldrite.plugin.spi.PluginDescriptor;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.util.jar.JarFile;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/** Bounded parallel JAR reads, ordered caller-thread descriptor parsing; no plugin classes are loaded. */
public final class PluginDiscovery {
    public record DiscoveredPlugin(Path jar,String sha256,PluginDescriptor descriptor) {}
    private record ScannedJar(Path path, String sha256, Map<String, String> documents) {}
    private final DescriptorReader reader;
    public PluginDiscovery(DescriptorReader reader){this.reader=Objects.requireNonNull(reader);}
    public List<DiscoveredPlugin> discover(Path directory)throws IOException{
        if(!Files.isDirectory(directory))throw new IOException("PLUGIN_DIRECTORY_UNAVAILABLE: "+directory);
        List<Path> jars;try(var files=Files.list(directory)){jars=files.filter(Files::isRegularFile).filter(p->p.getFileName().toString().endsWith(".jar")&&!p.getFileName().toString().startsWith(".")).sorted().toList();}
        List<String> names = List.copyOf(reader.descriptorNames());
        int threads = Math.min(jars.size(), scanThreads());
        List<DiscoveredPlugin> result = new ArrayList<>();
        if (threads <= 1) {
            for (Path path : jars) result.add(describe(scan(path, names), result.size()));
            return List.copyOf(result);
        }
        var ids = new AtomicInteger();
        var executor = new ThreadPoolExecutor(threads, threads, 0, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(threads), operation -> {
                    var thread = new Thread(operation, "Meldrite discovery worker #" + ids.incrementAndGet());
                    thread.setDaemon(true);
                    thread.setContextClassLoader(PluginDiscovery.class.getClassLoader());
                    return thread;
                }, new ThreadPoolExecutor.AbortPolicy());
        var pending = new ArrayDeque<Future<ScannedJar>>();
        Throwable failure = null;
        try {
            int next = 0;
            while (next < threads) {
                Path path = jars.get(next++);
                pending.addLast(executor.submit(() -> scan(path, names)));
            }
            while (!pending.isEmpty()) {
                ScannedJar scanned = await(pending.getFirst());
                pending.removeFirst();
                // Readers may be stateful. Parse on the caller in sorted discovery order.
                result.add(describe(scanned, result.size()));
                if (next < jars.size()) {
                    Path path = jars.get(next++);
                    pending.addLast(executor.submit(() -> scan(path, names)));
                }
            }
            return List.copyOf(result);
        } catch (IOException | RuntimeException | Error caught) {
            failure = caught;
            throw caught;
        } finally {
            for (var task : pending) task.cancel(true);
            executor.shutdownNow();
            try { terminate(executor); }
            catch (IOException cleanup) {
                if (failure == null) throw cleanup;
                failure.addSuppressed(cleanup);
            }
        }
    }
    private DiscoveredPlugin describe(ScannedJar jar, int index) {
        return new DiscoveredPlugin(jar.path.toAbsolutePath().normalize(), jar.sha256, reader.read(jar.path, jar.documents, index));
    }
    private static ScannedJar scan(Path path, List<String> names) throws IOException {
        checkInterrupted(path);
        try (JarFile jar = new JarFile(path.toFile(), true, java.util.zip.ZipFile.OPEN_READ, JarFile.runtimeVersion())) {
            Map<String, String> documents = new LinkedHashMap<>();
            for (String name : names) {
                var entry = jar.getJarEntry(name);
                if (entry == null) continue;
                try (var input = jar.getInputStream(entry)) {
                    byte[] bytes = input.readNBytes(1_048_577);
                    if (bytes.length > 1_048_576) throw new IOException("DESCRIPTOR_TOO_LARGE: " + path + "!" + name);
                    String text = StandardCharsets.UTF_8.newDecoder()
                            .onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT)
                            .decode(ByteBuffer.wrap(bytes)).toString();
                    documents.put(name, text);
                }
            }
            if (documents.isEmpty()) throw new IOException("PLUGIN_DESCRIPTOR_MISSING: " + path);
            return new ScannedJar(path, sha256(path), Collections.unmodifiableMap(documents));
        }
    }
    private static int scanThreads() {
        String value = System.getProperty("meldrite.discovery.threads", "0");
        final int threads;
        try { threads = Integer.parseInt(value.trim()); }
        catch (NumberFormatException invalid) { throw new IllegalArgumentException("meldrite.discovery.threads must be between 0 and 16", invalid); }
        if (threads < 0 || threads > 16) throw new IllegalArgumentException("meldrite.discovery.threads must be between 0 and 16");
        return threads == 0 ? Math.min(4, Math.max(1, Runtime.getRuntime().availableProcessors() - 1)) : threads;
    }
    private static ScannedJar await(Future<ScannedJar> task) throws IOException {
        try { return task.get(); }
        catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            var failure = new InterruptedIOException("PLUGIN_DISCOVERY_INTERRUPTED");
            failure.initCause(interrupted);
            throw failure;
        } catch (ExecutionException failed) {
            Throwable cause = failed.getCause();
            if (cause instanceof IOException io) throw io;
            if (cause instanceof RuntimeException runtime) throw runtime;
            if (cause instanceof Error error) throw error;
            throw new IOException("PLUGIN_DISCOVERY_FAILED", cause);
        }
    }
    private static void terminate(ThreadPoolExecutor executor) throws IOException {
        boolean interrupted = Thread.interrupted();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        try {
            while (!executor.isTerminated()) {
                long remaining = deadline - System.nanoTime();
                if (remaining <= 0) throw new IOException("PLUGIN_DISCOVERY_SHUTDOWN_TIMEOUT");
                try { executor.awaitTermination(remaining, TimeUnit.NANOSECONDS); }
                catch (InterruptedException caught) { interrupted = true; }
            }
        } finally { if (interrupted) Thread.currentThread().interrupt(); }
    }
    private static String sha256(Path file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (var stream = Files.newInputStream(file)) {
                byte[] bytes = new byte[8192];
                int count;
                while ((count = stream.read(bytes)) != -1) {
                    checkInterrupted(file);
                    digest.update(bytes, 0, count);
                }
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException impossible) { throw new IOException("SHA-256 unavailable", impossible); }
    }
    private static void checkInterrupted(Path file) throws InterruptedIOException {
        if (Thread.currentThread().isInterrupted()) throw new InterruptedIOException("PLUGIN_DISCOVERY_INTERRUPTED: " + file);
    }
}
