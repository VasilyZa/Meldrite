package dev.meldrite.paper.compat;

import dev.meldrite.plugin.spi.ClassTransform;
import dev.meldrite.plugin.spi.TransformContext;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/** Routes the public Paper world lookup ABI through the native lookup accessor. */
public final class EntityAbiTransform implements ClassTransform {
    @Override public byte[] apply(byte[] source, TransformContext context) {
        if (source.length < 10 || source[0] != (byte) 0xCA || source[1] != (byte) 0xFE
                || source[2] != (byte) 0xBA || source[3] != (byte) 0xBE) return source;
        var reader = new ClassReader(source);
        String plugin = context == null ? "" : context.pluginId();
        LegacyNameTransform.index(reader, plugin);
        var writer = new ClassWriter(0);
        reader.accept(new ClassVisitor(Opcodes.ASM9, writer) {
            @Override public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                return new MethodVisitor(Opcodes.ASM9, super.visitMethod(access, name, descriptor, signature, exceptions)) {
                    @Override public void visitMethodInsn(int opcode, String owner, String method, String desc, boolean isInterface) {
                        String mapped = LegacyMappings.methodName(plugin, owner, method, desc);
                        String lookup = LegacyMappings.methodName("", "net/minecraft/world/level/World", "E",
                                "()Lnet/minecraft/world/level/entity/LevelEntityGetter;");
                        if (opcode == Opcodes.INVOKEVIRTUAL && desc.startsWith("()L") && mapped.equals(lookup)
                                && LegacyMappings.isSubclass(plugin, owner, "net/minecraft/world/level/World")) {
                            super.visitMethodInsn(Opcodes.INVOKESTATIC, "dev/meldrite/v1201/compat/NativeEntityExtensions",
                                    "lookup", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
                            super.visitTypeInsn(Opcodes.CHECKCAST, Type.getReturnType(desc).getInternalName());
                            return;
                        }
                        super.visitMethodInsn(opcode, owner, method, desc, isInterface);
                    }
                };
            }
        }, 0);
        return writer.toByteArray();
    }
}
