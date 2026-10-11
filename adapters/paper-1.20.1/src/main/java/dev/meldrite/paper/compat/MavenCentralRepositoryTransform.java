package dev.meldrite.paper.compat;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

/** Patches Paper's resolver for the retired public OSSRH Central alias. */
public final class MavenCentralRepositoryTransform implements Opcodes {
    private static final String TYPE = "io/papermc/paper/plugin/loader/library/impl/MavenLibraryResolver";
    private static final String REPOSITORY = "org/eclipse/aether/repository/RemoteRepository";
    private static final String BUILDER = REPOSITORY + "$Builder";
    private static final String REPOSITORY_DESC = "L" + REPOSITORY + ";";
    private static final String NORMALIZER = "meldrite$normalizeRepository";
    private static final String CANONICAL = "https://repo.maven.apache.org/maven2/";
    private static final String RETIRED = "https://oss.sonatype.org/content/repositories/releases";
    private static final String RETIRED_TRAILING = RETIRED + "/";

    private MavenCentralRepositoryTransform() { }

    public static byte[] transform(byte[] source) {
        ClassReader reader = new ClassReader(source);
        if (!TYPE.equals(reader.getClassName())) return source;
        boolean[] alreadyPatched = { false };
        reader.accept(new ClassVisitor(ASM9) {
            @Override public MethodVisitor visitMethod(int access, String name, String descriptor,
                    String signature, String[] exceptions) {
                if (NORMALIZER.equals(name)) alreadyPatched[0] = true;
                return null;
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        if (alreadyPatched[0]) return source;

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        reader.accept(new ClassVisitor(ASM9, writer) {
            @Override public MethodVisitor visitMethod(int access, String name, String descriptor,
                    String signature, String[] exceptions) {
                MethodVisitor method = super.visitMethod(access, name, descriptor, signature, exceptions);
                if (name.equals("addRepository") && descriptor.equals("(" + REPOSITORY_DESC + ")V")) {
                    return new MethodVisitor(ASM9, method) {
                        @Override public void visitCode() {
                            super.visitCode();
                            visitVarInsn(ALOAD, 1);
                            visitMethodInsn(INVOKESTATIC, TYPE, NORMALIZER,
                                    "(" + REPOSITORY_DESC + ")" + REPOSITORY_DESC, false);
                            visitVarInsn(ASTORE, 1);
                        }
                    };
                }
                return method;
            }

            @Override public void visitEnd() {
                addNormalizer(this);
                super.visitEnd();
            }
        }, ClassReader.EXPAND_FRAMES);
        return writer.toByteArray();
    }

    private static void addNormalizer(ClassVisitor visitor) {
        String descriptor = "(" + REPOSITORY_DESC + ")" + REPOSITORY_DESC;
        MethodVisitor method = visitor.visitMethod(ACC_PRIVATE | ACC_STATIC, NORMALIZER, descriptor, null, null);
        method.visitCode();
        method.visitVarInsn(ALOAD, 0);
        method.visitMethodInsn(INVOKEVIRTUAL, REPOSITORY, "getUrl", "()Ljava/lang/String;", false);
        method.visitVarInsn(ASTORE, 1);
        method.visitVarInsn(ALOAD, 1);
        method.visitLdcInsn(RETIRED);
        method.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
        org.objectweb.asm.Label checkTrailing = new org.objectweb.asm.Label();
        org.objectweb.asm.Label keep = new org.objectweb.asm.Label();
        method.visitJumpInsn(IFEQ, checkTrailing);
        addCanonicalRepository(method);
        method.visitLabel(checkTrailing);
        method.visitVarInsn(ALOAD, 1);
        method.visitLdcInsn(RETIRED_TRAILING);
        method.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
        method.visitJumpInsn(IFEQ, keep);
        addCanonicalRepository(method);
        method.visitLabel(keep);
        method.visitVarInsn(ALOAD, 0);
        method.visitInsn(ARETURN);
        method.visitMaxs(0, 0);
        method.visitEnd();
    }

    private static void addCanonicalRepository(MethodVisitor method) {
        method.visitTypeInsn(NEW, BUILDER);
        method.visitInsn(DUP);
        method.visitVarInsn(ALOAD, 0);
        method.visitMethodInsn(INVOKESPECIAL, BUILDER, "<init>", "(" + REPOSITORY_DESC + ")V", false);
        method.visitLdcInsn(CANONICAL);
        method.visitMethodInsn(INVOKEVIRTUAL, BUILDER, "setUrl", "(Ljava/lang/String;)" + "L" + BUILDER + ";", false);
        method.visitMethodInsn(INVOKEVIRTUAL, BUILDER, "build", "()" + REPOSITORY_DESC, false);
        method.visitInsn(ARETURN);
    }
}
