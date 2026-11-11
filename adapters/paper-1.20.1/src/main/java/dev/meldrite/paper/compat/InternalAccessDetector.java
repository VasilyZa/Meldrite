package dev.meldrite.paper.compat;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.RecordComponentVisitor;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

/** Classifies actual bytecode and reflection constants before choosing a plugin's access mode. */
public final class InternalAccessDetector {
    private InternalAccessDetector() {}

    // ClassRemapper skips methods/fields/annotations when its delegate returns
    // null. Visit every reference, including nested annotation values and method
    // instructions, but discard output instead of rebuilding each class file.
    // These visitors are stateless and can be shared by independent scans.
    private static final AnnotationVisitor ANNOTATIONS = new AnnotationVisitor(Opcodes.ASM9) {
        @Override public AnnotationVisitor visitAnnotation(String name, String descriptor) { return this; }
        @Override public AnnotationVisitor visitArray(String name) { return this; }
    };
    private static final FieldVisitor FIELDS = new FieldVisitor(Opcodes.ASM9) {
        @Override public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) { return ANNOTATIONS; }
        @Override public AnnotationVisitor visitTypeAnnotation(int reference, TypePath path, String descriptor, boolean visible) { return ANNOTATIONS; }
    };
    private static final RecordComponentVisitor RECORD_COMPONENTS = new RecordComponentVisitor(Opcodes.ASM9) {
        @Override public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) { return ANNOTATIONS; }
        @Override public AnnotationVisitor visitTypeAnnotation(int reference, TypePath path, String descriptor, boolean visible) { return ANNOTATIONS; }
    };
    private static final MethodVisitor METHODS = new MethodVisitor(Opcodes.ASM9) {
        @Override public AnnotationVisitor visitAnnotationDefault() { return ANNOTATIONS; }
        @Override public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) { return ANNOTATIONS; }
        @Override public AnnotationVisitor visitTypeAnnotation(int reference, TypePath path, String descriptor, boolean visible) { return ANNOTATIONS; }
        @Override public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) { return ANNOTATIONS; }
        @Override public AnnotationVisitor visitInsnAnnotation(int reference, TypePath path, String descriptor, boolean visible) { return ANNOTATIONS; }
        @Override public AnnotationVisitor visitTryCatchAnnotation(int reference, TypePath path, String descriptor, boolean visible) { return ANNOTATIONS; }
        @Override public AnnotationVisitor visitLocalVariableAnnotation(int reference, TypePath path, org.objectweb.asm.Label[] start, org.objectweb.asm.Label[] end, int[] index, String descriptor, boolean visible) { return ANNOTATIONS; }
    };
    private static final ModuleVisitor MODULE = new ModuleVisitor(Opcodes.ASM9) {};
    private static final ClassVisitor REFERENCES = new ClassVisitor(Opcodes.ASM9) {
        @Override public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) { return ANNOTATIONS; }
        @Override public AnnotationVisitor visitTypeAnnotation(int reference, TypePath path, String descriptor, boolean visible) { return ANNOTATIONS; }
        @Override public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) { return FIELDS; }
        @Override public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) { return METHODS; }
        @Override public RecordComponentVisitor visitRecordComponent(String name, String descriptor, String signature) { return RECORD_COMPONENTS; }
        @Override public ModuleVisitor visitModule(String name, int access, String version) { return MODULE; }
    };
    private static final class ReferenceDetector extends Remapper {
        // A class commonly repeats the same API owner in thousands of calls.
        // Deduplicate only within this class; no result survives a JAR scan.
        private final Set<String> inspected = new HashSet<>();
        private boolean found;
        private void inspect(String name) {
            if (!found && inspected.add(name)) found = internal(name);
        }
        @Override public String map(String name) { inspect(name); return name; }
        @Override public Object mapValue(Object value) {
            if (value instanceof String text) inspect(text);
            return super.mapValue(value);
        }
    }

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
    public static void indexLibrary(String ownerId, Path library) throws IOException {
        index(ownerId, library);
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
                    var detector = new ReferenceDetector();
                    try (var input = jar.getInputStream(entry)) {
                        new ClassReader(input).accept(new ClassRemapper(
                                REFERENCES, detector), ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                    }
                    if (detector.found) return true;
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
                || !LegacyNameTransform.reflectionClassName(dotted).equals(dotted);
    }
}
