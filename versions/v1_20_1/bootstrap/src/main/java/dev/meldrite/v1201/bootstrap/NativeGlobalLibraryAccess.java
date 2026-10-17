package dev.meldrite.v1201.bootstrap;

import dev.meldrite.plugin.classloading.JvmClassPath;
import dev.meldrite.plugin.classloading.RuntimeInstrumentation;
import dev.meldrite.plugin.spi.GlobalLibraryAccess;
import dev.meldrite.plugin.spi.ClassTransform;
import dev.meldrite.plugin.spi.TransformContext;
import java.io.IOException;
import java.lang.instrument.*;
import java.nio.file.Path;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import org.bukkit.Bukkit;

/** Prepared definitions enter the actual Fabric runtime, retaining one global class identity. */
final class NativeGlobalLibraryAccess implements GlobalLibraryAccess, ClassFileTransformer {
    private record Definition(byte[] original, byte[] prepared, ClassTransform transform, TransformContext context) {}
    private final Map<String, Map<String, Definition>> definitions = new ConcurrentHashMap<>();
    private Instrumentation instrumentation;
    @Override public ClassLoader sourceLoader() { return Bukkit.class.getClassLoader(); }
    @Override public List<Path> paths() throws IOException { return JvmClassPath.paths(sourceLoader()); }
    @Override public ClassLoader runtimeLoader() { return NativeGlobalLibraryAccess.class.getClassLoader(); }
    @Override public synchronized void append(Path source, Path prepared, ClassTransform transformer, TransformContext context) throws IOException {
        Map<String, Definition> classes = new HashMap<>();
        try (var jar = new JarFile(prepared.toFile()); var raw = new JarFile(source.toFile(),true,JarFile.OPEN_READ,Runtime.version())) {
            for (var entry : jar.stream().filter(e -> !e.isDirectory() && e.getName().endsWith(".class") && !e.getName().equals("module-info.class")).toList()) {
                String name = entry.getName().substring(0, entry.getName().length()-6);
                if (FabricLauncherBase.getLauncher().isClassLoaded(name.replace('/', '.')))
                    throw new IOException("GLOBAL_LIBRARY_ALREADY_DEFINED: " + name + " from " + source);
                var original = raw.getJarEntry(entry.getName());
                if (original == null) throw new IOException("GLOBAL_LIBRARY_SOURCE_ENTRY_MISSING: " + entry.getName());
                try (var input = jar.getInputStream(entry); var originalInput = raw.getInputStream(original)) {
                    classes.put(name, new Definition(originalInput.readAllBytes(),input.readAllBytes(),transformer,context));
                }
            }
        }
        if (instrumentation == null) {
            instrumentation = RuntimeInstrumentation.require();
            instrumentation.addTransformer(this);
        }
        String location = source.toUri().toURL().toExternalForm();
        definitions.put(location, Map.copyOf(classes));
        try { FabricLauncherBase.getLauncher().addToClassPath(source); }
        catch (RuntimeException | Error failure) { definitions.remove(location); throw failure; }
    }
    @Override public byte[] transform(ClassLoader loader, String name, Class<?> redefined, ProtectionDomain domain, byte[] original) {
        if (name == null || loader != runtimeLoader() || domain == null || domain.getCodeSource() == null || domain.getCodeSource().getLocation() == null) return null;
        var classes = definitions.get(domain.getCodeSource().getLocation().toExternalForm());
        if (classes == null) return null;
        Definition definition = classes.get(name);
        if (definition == null) return null;
        if (Arrays.equals(original, definition.original())) return definition.prepared().clone();
        if (Arrays.equals(original, definition.prepared())) return null;
        // Preserve earlier agent/Mixin edits instead of replacing them with cached original code.
        try {
            return Objects.requireNonNull(definition.transform().apply(
                    Objects.requireNonNull(definition.transform().prepare(original.clone(), definition.context())), definition.context()));
        } catch (Exception | LinkageError failure) {
            System.getLogger("Meldrite.GlobalLibraries").log(System.Logger.Level.ERROR,
                    "GLOBAL_LIBRARY_TRANSFORMATION_FAILED: " + name, failure);
            // Instrumentation ignores thrown transformer exceptions. An invalid definition
            // makes this failure visible as a ClassFormatError instead of defining raw code.
            return new byte[0];
        }
    }
    @Override public synchronized void close() {
        if (instrumentation != null) {
            instrumentation.removeTransformer(this);
            instrumentation = null;
        }
        definitions.clear();
    }
}
