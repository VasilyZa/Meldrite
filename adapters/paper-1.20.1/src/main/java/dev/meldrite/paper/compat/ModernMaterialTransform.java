package dev.meldrite.paper.compat;

import dev.meldrite.plugin.spi.ClassTransform;
import dev.meldrite.plugin.spi.TransformContext;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/** Paper's modern Material API conversion, selected from the plugin descriptor. */
public final class ModernMaterialTransform implements ClassTransform {
    @Override public byte[] apply(byte[] source, TransformContext context) {
        var reader = new ClassReader(source);
        var writer = new ClassWriter(reader, 0);
        boolean[] changed = {false};
        reader.accept(new ClassVisitor(Opcodes.ASM9, writer) {
            @Override public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                return new MethodVisitor(Opcodes.ASM9, super.visitMethod(access, name, descriptor, signature, exceptions)) {
                    @Override public void visitMethodInsn(int opcode, String owner, String method, String desc, boolean isInterface) {
                        if (owner.equals("org/bukkit/Material")) {
                            if (opcode == Opcodes.INVOKESTATIC && method.equals("values") && desc.equals("()[Lorg/bukkit/Material;")) {
                                changed[0] = true;
                                super.visitMethodInsn(opcode, "org/bukkit/craftbukkit/v1_20_R1/util/CraftLegacy", "modern_values", desc, false);
                                return;
                            }
                            if (opcode == Opcodes.INVOKEVIRTUAL && method.equals("ordinal") && desc.equals("()I")) {
                                changed[0] = true;
                                super.visitMethodInsn(Opcodes.INVOKESTATIC, "org/bukkit/craftbukkit/v1_20_R1/util/CraftLegacy", "modern_ordinal", "(Lorg/bukkit/Material;)I", false);
                                return;
                            }
                        }
                        super.visitMethodInsn(opcode, owner, method, desc, isInterface);
                    }
                };
            }
        }, 0);
        return changed[0] ? writer.toByteArray() : source;
    }
}
