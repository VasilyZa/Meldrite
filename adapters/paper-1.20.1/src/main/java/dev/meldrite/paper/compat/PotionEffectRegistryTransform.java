package dev.meldrite.paper.compat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/** Keeps Paper 1.20.1's effect ABI while indexing native effects beyond its fixed array. */
public final class PotionEffectRegistryTransform implements Opcodes {
    private static final String TYPE = "org/bukkit/potion/PotionEffectType";
    private static final String VALUE = "L" + TYPE + ";";
    private static final String ARRAY = "[" + VALUE;
    private static final String OVERFLOW = "meldrite$effectsById";
    private PotionEffectRegistryTransform() { }

    public static void main(String[] arguments) throws IOException {
        if (arguments.length != 2) throw new IllegalArgumentException("Expected source Paper API jar and projected output jar");
        projectJar(Path.of(arguments[0]), Path.of(arguments[1]));
    }

    public static void projectJar(Path source, Path output) throws IOException {
        if (source.toAbsolutePath().normalize().equals(output.toAbsolutePath().normalize())) throw new IllegalArgumentException("Projection must not overwrite the contract artifact");
        byte[] target;
        try (JarFile input = new JarFile(source.toFile())) {
            if (input.stream().map(JarEntry::getName).anyMatch(name -> name.startsWith("META-INF/") && (name.endsWith(".SF") || name.endsWith(".RSA") || name.endsWith(".DSA")))) throw new IOException("Signed API artifact requires an explicit projection signing policy");
            JarEntry effect = input.getJarEntry(TYPE + ".class");
            if (effect == null) throw new IOException("Paper API does not contain " + TYPE);
            try (var bytes = input.getInputStream(effect)) { target = transform(bytes.readAllBytes()); }
            JarEntry resolver = input.getJarEntry("io/papermc/paper/plugin/loader/library/impl/MavenLibraryResolver.class");
            byte[] resolverTarget = null;
            if (resolver != null) try (var bytes = input.getInputStream(resolver)) {
                resolverTarget = MavenCentralRepositoryTransform.transform(bytes.readAllBytes());
            }
            Path parent = output.toAbsolutePath().getParent(); if (parent != null) Files.createDirectories(parent);
            try (JarOutputStream projected = new JarOutputStream(Files.newOutputStream(output))) {
                for (var entries = input.entries(); entries.hasMoreElements();) {
                    JarEntry original = entries.nextElement();
                    String name = original.getName();
                    JarEntry entry = new JarEntry(name); entry.setTime(original.getTime()); projected.putNextEntry(entry);
                    if (!original.isDirectory()) {
                        if (name.equals(TYPE + ".class")) projected.write(target);
                        else if (name.equals("org/bukkit/map/MapPalette.class")) {
                            try (var bytes = input.getInputStream(original)) {
                                projected.write(MapPaletteImageTransform.transform(bytes.readAllBytes()));
                            }
                        }
                        else if (name.equals("io/papermc/paper/plugin/loader/library/impl/MavenLibraryResolver.class") && resolverTarget != null) projected.write(resolverTarget);
                        else try (var bytes = input.getInputStream(original)) { bytes.transferTo(projected); }
                    }
                    projected.closeEntry();
                }
            }
        }
    }

    public static byte[] transform(byte[] source) {
        ClassReader reader = new ClassReader(source);
        if (!TYPE.equals(reader.getClassName())) return source;
        boolean[] patched = { false };
        reader.accept(new ClassVisitor(ASM9) {
            @Override public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                if (OVERFLOW.equals(name)) patched[0] = true;
                return null;
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        if (patched[0]) return source;
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        reader.accept(new ClassVisitor(ASM9, writer) {
            @Override public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                MethodVisitor method = super.visitMethod(access, name, descriptor, signature, exceptions);
                if (name.equals("getById") && descriptor.equals("(I)" + VALUE)) { lookup(method); return null; }
                if (name.equals("registerPotionEffectType") && descriptor.equals("(" + VALUE + ")V")) { register(method); return null; }
                if (name.equals("values") && descriptor.equals("()" + ARRAY)) { values(method); return null; }
                if (name.equals("<clinit>")) return new MethodVisitor(ASM9, method) {
                    @Override public void visitInsn(int opcode) {
                        if (opcode == RETURN) {
                            visitTypeInsn(NEW, "java/util/TreeMap"); visitInsn(DUP);
                            visitMethodInsn(INVOKESPECIAL, "java/util/TreeMap", "<init>", "()V", false);
                            visitFieldInsn(PUTSTATIC, TYPE, OVERFLOW, "Ljava/util/Map;");
                        }
                        super.visitInsn(opcode);
                    }
                };
                return method;
            }
            @Override public void visitEnd() {
                super.visitField(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, OVERFLOW, "Ljava/util/Map;", "Ljava/util/Map<Ljava/lang/Integer;" + VALUE + ">;", null).visitEnd();
                super.visitEnd();
            }
        }, ClassReader.EXPAND_FRAMES);
        return writer.toByteArray();
    }

    private static void lookup(MethodVisitor method) {
        method.visitCode(); Label positive = new Label(), overflow = new Label();
        method.visitVarInsn(ILOAD, 0); method.visitJumpInsn(IFGE, positive); method.visitInsn(ACONST_NULL); method.visitInsn(ARETURN);
        method.visitLabel(positive); method.visitVarInsn(ILOAD, 0); method.visitFieldInsn(GETSTATIC, TYPE, "byId", ARRAY);
        method.visitInsn(ARRAYLENGTH); method.visitJumpInsn(IF_ICMPGE, overflow);
        method.visitFieldInsn(GETSTATIC, TYPE, "byId", ARRAY); method.visitVarInsn(ILOAD, 0); method.visitInsn(AALOAD); method.visitInsn(ARETURN);
        method.visitLabel(overflow); method.visitFieldInsn(GETSTATIC, TYPE, OVERFLOW, "Ljava/util/Map;");
        method.visitVarInsn(ILOAD, 0); method.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
        method.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", true);
        method.visitTypeInsn(CHECKCAST, TYPE); method.visitInsn(ARETURN); method.visitMaxs(0, 0); method.visitEnd();
    }

    private static void register(MethodVisitor method) {
        method.visitCode(); Label valid = new Label(), duplicate = new Label(), unique = new Label(), accepting = new Label(), overflow = new Label(), indexed = new Label();
        method.visitVarInsn(ALOAD, 0); method.visitFieldInsn(GETFIELD, TYPE, "id", "I"); method.visitJumpInsn(IFGE, valid);
        failure(method, "java/lang/IllegalArgumentException", "Potion effect id must be non-negative");
        method.visitLabel(valid); method.visitVarInsn(ALOAD, 0); method.visitFieldInsn(GETFIELD, TYPE, "id", "I");
        method.visitMethodInsn(INVOKESTATIC, TYPE, "getById", "(I)" + VALUE, false); method.visitJumpInsn(IFNONNULL, duplicate);
        method.visitFieldInsn(GETSTATIC, TYPE, "byName", "Ljava/util/Map;"); effectName(method);
        method.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "containsKey", "(Ljava/lang/Object;)Z", true); method.visitJumpInsn(IFNE, duplicate);
        method.visitFieldInsn(GETSTATIC, TYPE, "byKey", "Ljava/util/Map;"); effectKey(method);
        method.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "containsKey", "(Ljava/lang/Object;)Z", true); method.visitJumpInsn(IFEQ, unique);
        method.visitLabel(duplicate); failure(method, "java/lang/IllegalArgumentException", "Cannot set already-set type");
        method.visitLabel(unique); method.visitFieldInsn(GETSTATIC, TYPE, "acceptingNew", "Z"); method.visitJumpInsn(IFNE, accepting);
        failure(method, "java/lang/IllegalStateException", "No longer accepting new potion effect types (can only be done by the server implementation)");
        method.visitLabel(accepting); method.visitVarInsn(ALOAD, 0); method.visitFieldInsn(GETFIELD, TYPE, "id", "I");
        method.visitFieldInsn(GETSTATIC, TYPE, "byId", ARRAY); method.visitInsn(ARRAYLENGTH); method.visitJumpInsn(IF_ICMPGE, overflow);
        method.visitFieldInsn(GETSTATIC, TYPE, "byId", ARRAY); method.visitVarInsn(ALOAD, 0); method.visitFieldInsn(GETFIELD, TYPE, "id", "I");
        method.visitVarInsn(ALOAD, 0); method.visitInsn(AASTORE); method.visitJumpInsn(GOTO, indexed);
        method.visitLabel(overflow); method.visitFieldInsn(GETSTATIC, TYPE, OVERFLOW, "Ljava/util/Map;");
        method.visitVarInsn(ALOAD, 0); method.visitFieldInsn(GETFIELD, TYPE, "id", "I"); method.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
        method.visitVarInsn(ALOAD, 0); put(method);
        method.visitLabel(indexed); method.visitFieldInsn(GETSTATIC, TYPE, "byName", "Ljava/util/Map;"); effectName(method); method.visitVarInsn(ALOAD, 0); put(method);
        method.visitFieldInsn(GETSTATIC, TYPE, "byKey", "Ljava/util/Map;"); effectKey(method); method.visitVarInsn(ALOAD, 0); put(method);
        method.visitInsn(RETURN); method.visitMaxs(0, 0); method.visitEnd();
    }

    private static void values(MethodVisitor method) {
        method.visitCode();
        method.visitFieldInsn(GETSTATIC, TYPE, "byId", ARRAY); method.visitInsn(ICONST_1);
        method.visitFieldInsn(GETSTATIC, TYPE, "byId", ARRAY); method.visitInsn(ARRAYLENGTH);
        method.visitMethodInsn(INVOKESTATIC, "java/util/Arrays", "copyOfRange", "([Ljava/lang/Object;II)[Ljava/lang/Object;", false);
        method.visitTypeInsn(CHECKCAST, ARRAY); method.visitVarInsn(ASTORE, 0);
        method.visitVarInsn(ALOAD, 0); method.visitVarInsn(ALOAD, 0); method.visitInsn(ARRAYLENGTH);
        method.visitFieldInsn(GETSTATIC, TYPE, OVERFLOW, "Ljava/util/Map;"); method.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "size", "()I", true);
        method.visitInsn(IADD); method.visitMethodInsn(INVOKESTATIC, "java/util/Arrays", "copyOf", "([Ljava/lang/Object;I)[Ljava/lang/Object;", false);
        method.visitTypeInsn(CHECKCAST, ARRAY); method.visitVarInsn(ASTORE, 1);
        method.visitFieldInsn(GETSTATIC, TYPE, OVERFLOW, "Ljava/util/Map;"); method.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "values", "()Ljava/util/Collection;", true);
        method.visitMethodInsn(INVOKEINTERFACE, "java/util/Collection", "toArray", "()[Ljava/lang/Object;", true); method.visitVarInsn(ASTORE, 2);
        method.visitVarInsn(ALOAD, 2); method.visitInsn(ICONST_0); method.visitVarInsn(ALOAD, 1); method.visitVarInsn(ALOAD, 0); method.visitInsn(ARRAYLENGTH);
        method.visitVarInsn(ALOAD, 2); method.visitInsn(ARRAYLENGTH); method.visitMethodInsn(INVOKESTATIC, "java/lang/System", "arraycopy", "(Ljava/lang/Object;ILjava/lang/Object;II)V", false);
        method.visitVarInsn(ALOAD, 1); method.visitInsn(ARETURN); method.visitMaxs(0, 0); method.visitEnd();
    }

    private static void effectName(MethodVisitor method) {
        method.visitVarInsn(ALOAD, 0); method.visitMethodInsn(INVOKEVIRTUAL, TYPE, "getName", "()Ljava/lang/String;", false);
        method.visitFieldInsn(GETSTATIC, "java/util/Locale", "ENGLISH", "Ljava/util/Locale;");
        method.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "toLowerCase", "(Ljava/util/Locale;)Ljava/lang/String;", false);
    }
    private static void effectKey(MethodVisitor method) { method.visitVarInsn(ALOAD, 0); method.visitFieldInsn(GETFIELD, TYPE, "key", "Lorg/bukkit/NamespacedKey;"); }
    private static void put(MethodVisitor method) { method.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true); method.visitInsn(POP); }
    private static void failure(MethodVisitor method, String type, String message) {
        method.visitTypeInsn(NEW, type); method.visitInsn(DUP); method.visitLdcInsn(message);
        method.visitMethodInsn(INVOKESPECIAL, type, "<init>", "(Ljava/lang/String;)V", false); method.visitInsn(ATHROW);
    }
}
