package dev.meldrite.paper.compat;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

/** Classifies actual bytecode and reflection constants before choosing a plugin's access mode. */
public final class InternalAccessDetector {
    private InternalAccessDetector() {}

    public static boolean requiresRemapping(Path plugin, List<Path> libraries) throws IOException {
        if (containsInternalAccess(plugin)) return true;
        for (Path library : libraries) if (containsInternalAccess(library)) return true;
        return false;
    }

    public static boolean requiresRemapping(String pluginId, Path plugin, List<Path> libraries) throws IOException {
        boolean internal = requiresRemapping(plugin, libraries);
        if (internal) prepareMappings(pluginId, plugin, libraries);
        return internal;
    }

    public static void prepareMappings(String pluginId, Path plugin, List<Path> libraries) throws IOException {
        LegacyMappings.clearPlugin(pluginId);
        for (Path library : libraries) index(pluginId, library);
        index(pluginId, plugin);
    }

    private static void index(String pluginId, Path path) throws IOException {
        try (JarFile jar = new JarFile(path.toFile())) {
            for (var entry : jar.stream().filter(entry -> !entry.isDirectory() && entry.getName().endsWith(".class")).toList()) {
                try (var input = jar.getInputStream(entry)) {
                    LegacyNameTransform.index(new ClassReader(input), pluginId);
                }
            }
        }
    }

    private static boolean containsInternalAccess(Path path) throws IOException {
        try (JarFile jar = new JarFile(path.toFile())) {
            var entries = jar.entries();
            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                    boolean[] found = {false};
                    var detector = new Remapper() {
                        @Override public String map(String name) {
                            if (internal(name)) found[0] = true;
                            return name;
                        }
                        @Override public Object mapValue(Object value) {
                            if (value instanceof String text && internal(text)) found[0] = true;
                            return super.mapValue(value);
                        }
                    };
                    try (var input = jar.getInputStream(entry)) {
                        new ClassReader(input).accept(new ClassRemapper(
                                new ClassWriter(0), detector), ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                    }
                    if (found[0]) return true;
                }
            }
        }
        return false;
    }

    private static boolean internal(String name) {
        String dotted = name.replace('/', '.');
        if (dotted.startsWith("[")) dotted = dotted.replaceFirst("^\\[+L?", "");
        return dotted.startsWith("net.minecraft.") || dotted.startsWith("org.bukkit.craftbukkit.")
                || dotted.equals("org.bukkit.craftbukkit") || dotted.equals("net.minecraft")
                || !LegacyMappings.className(name.replace('.', '/')).equals(name.replace('.', '/'));
    }
}
