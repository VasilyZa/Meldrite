package dev.meldrite.plugin.classloading;

import dev.meldrite.plugin.spi.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.util.jar.*;

/** Server-scoped definitions for explicitly appended host JARs, never initial host internals. */
final class GlobalLibraryRegistry implements AutoCloseable {
    private final ApiAuthority authority;
    private final ClassTransform transform;
    private final GlobalLibraryAccess access;
    private final Path cache;
    private final Set<Path> seen = new HashSet<>();
    private final Set<String> classes = new HashSet<>();
    private final Resources resources = new Resources();
    private final AutoCloseable reflection;
    private boolean closed;
    private boolean retired;
    private int users = 1;
    private static final class Resources extends URLClassLoader {
        Resources() { super(new URL[0], ClassLoader.getPlatformClassLoader()); }
        void append(Path jar) throws MalformedURLException { super.addURL(jar.toUri().toURL()); }
    }
    GlobalLibraryRegistry(ApiAuthority authority, ClassTransform transform, GlobalLibraryAccess access, Path cache) throws IOException {
        this.authority = authority; this.transform = transform; this.access = Objects.requireNonNull(access);
        this.cache = cache.toAbsolutePath().normalize();
        seen.addAll(access.paths().stream().map(p -> p.toAbsolutePath().normalize()).toList());
        reflection = LegacyClassLookup.bindGlobal(access.sourceLoader(), this::find);
    }
    private synchronized void refresh() throws IOException {
        if (closed) throw new IOException("GLOBAL_LIBRARY_REGISTRY_CLOSED");
        for (Path candidate : access.paths()) {
            Path source = candidate.toAbsolutePath().normalize();
            if (seen.contains(source)) continue;
            authority.validateGlobal(source);
            Path prepared = prepare(source);
            Set<String> added = new HashSet<>();
            try (JarFile jar = new JarFile(prepared.toFile())) {
                jar.stream().filter(e -> !e.isDirectory() && e.getName().endsWith(".class") && !e.getName().equals("module-info.class"))
                        .forEach(e -> added.add(e.getName().substring(0, e.getName().length()-6).replace('/', '.')));
            }
            access.append(source, prepared, transform, new TransformContext("global:" + digest(source), TransformContext.Access.INTERNAL));
            resources.append(prepared);
            classes.addAll(added);
            seen.add(source);
        }
    }
    private Path prepare(Path source) throws IOException {
        Files.createDirectories(cache);
        Path temporary = Files.createTempFile(cache, "prepare-", ".jar");
        String id = "global:" + digest(source);
        var context = new TransformContext(id, TransformContext.Access.INTERNAL);
        try {
            transform.prepareLibrary(source, context);
            try (JarFile jar = new JarFile(source.toFile(), true, JarFile.OPEN_READ, Runtime.version());
                    var out = new JarOutputStream(Files.newOutputStream(temporary))) {
                for (JarEntry entry : jar.versionedStream().filter(e -> !e.isDirectory()).toList()) {
                    byte[] original;
                    try (var input = jar.getInputStream(entry)) { original = input.readAllBytes(); }
                    byte[] prepared = original;
                    if (entry.getName().endsWith(".class") && !entry.getName().equals("module-info.class")) {
                        prepared = Objects.requireNonNull(transform.apply(Objects.requireNonNull(transform.prepare(original.clone(), context)), context));
                        if (entry.getCertificates() != null && !Arrays.equals(original, prepared))
                            throw new SecurityException("Signed class transformation requires an explicit signature policy: " + source + "!" + entry.getName());
                    }
                    JarEntry copy = new JarEntry(entry.getName()); copy.setTime(0);
                    out.putNextEntry(copy); out.write(prepared); out.closeEntry();
                }
            }
            Path destination = cache.resolve(digest(temporary) + ".jar");
            if (Files.exists(destination)) {
                if (!digest(destination).equals(digest(temporary))) throw new IOException("GLOBAL_LIBRARY_CACHE_CORRUPT: " + destination);
            } else Files.move(temporary, destination, StandardCopyOption.ATOMIC_MOVE);
            return destination;
        } catch (IOException | RuntimeException failure) { throw failure; }
        catch (Exception failure) { throw new IOException("GLOBAL_LIBRARY_PREPARATION_FAILED: " + source, failure); }
        finally { Files.deleteIfExists(temporary); }
    }
    private static String digest(Path path) throws IOException {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            try (var input = Files.newInputStream(path)) { byte[] buffer = new byte[8192]; int count; while ((count = input.read(buffer)) >= 0) if (count > 0) digest.update(buffer, 0, count); }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException failure) { throw new IllegalStateException("SHA-256 unavailable", failure); }
    }
    Optional<Class<?>> find(String name, boolean initialize) throws ClassNotFoundException {
        try { refresh(); }
        catch (IOException failure) { throw new ClassNotFoundException("GLOBAL_LIBRARY_DISCOVERY_FAILED: " + name, failure); }
        synchronized (this) { if (!classes.contains(name)) return Optional.empty(); }
        return Optional.of(Class.forName(name, initialize, access.runtimeLoader()));
    }
    synchronized List<URL> resources(String name) throws IOException {
        refresh(); return Collections.list(resources.findResources(name));
    }
    synchronized void retain() {
        if (retired || closed) throw new IllegalStateException("GLOBAL_LIBRARY_REGISTRY_RETIRED");
        users++;
    }
    synchronized void release() throws IOException {
        if (--users == 0) closeResources();
    }
    @Override public synchronized void close() throws IOException {
        if (retired) return;
        retired = true;
        release();
    }
    private void closeResources() throws IOException {
        if (closed) return;
        closed = true;
        try { reflection.close(); }
        catch (Exception failure) { throw new IOException("GLOBAL_LIBRARY_LOOKUP_CLOSE_FAILED", failure); }
        finally {
            classes.clear(); seen.clear();
            try { resources.close(); } finally { access.close(); }
        }
    }
}
