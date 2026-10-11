package dev.meldrite.paper.compat;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/** Adds an optional image conversion path without replacing plugin color caches or API methods. */
public final class MapPaletteImageTransform implements Opcodes {
    private static final String TYPE = "org/bukkit/map/MapPalette";
    private MapPaletteImageTransform() { }

    public static byte[] transform(byte[] source) {
        var reader = new ClassReader(source);
        if (!TYPE.equals(reader.getClassName())) return source;
        var writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        reader.accept(new ClassVisitor(ASM9, writer) {
            @Override public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                var method = super.visitMethod(access, name, descriptor, signature, exceptions);
                if (!name.equals("imageToBytes") || !descriptor.equals("(Ljava/awt/Image;)[B")) return method;
                return new MethodVisitor(ASM9, method) {
                    @Override public void visitCode() {
                        super.visitCode();
                        var scalar = new Label();
                        var unavailable = new Label();
                        // A plugin-provided cache owns matching, even if it is temporarily inactive.
                        visitFieldInsn(GETSTATIC, TYPE, "mapColorCache", "Lorg/bukkit/map/MapPalette$MapColorCache;");
                        visitJumpInsn(IFNONNULL, scalar);
                        visitVarInsn(ALOAD, 0);
                        visitFieldInsn(GETSTATIC, TYPE, "colors", "[Ljava/awt/Color;");
                        visitMethodInsn(INVOKESTATIC, "dev/meldrite/paper/map/VectorMapColors", "imageToBytes",
                                "(Ljava/awt/Image;[Ljava/awt/Color;)[B", false);
                        visitInsn(DUP);
                        visitJumpInsn(IFNULL, unavailable);
                        visitInsn(ARETURN);
                        visitLabel(unavailable);
                        visitInsn(POP);
                        visitLabel(scalar);
                    }
                };
            }
        }, ClassReader.EXPAND_FRAMES);
        return writer.toByteArray();
    }
}
