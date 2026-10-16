package dev.meldrite.paper.compat;

import dev.meldrite.plugin.spi.ClassTransform;
import dev.meldrite.plugin.spi.TransformContext;
import org.bukkit.Location;
import org.bukkit.RegionAccessor;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/** Adapts the Java Consumer spawn ABI to the locked Paper API's Bukkit Consumer. */
public final class WorldSpawnAbiTransform implements ClassTransform {
    private static final String BRIDGE = "dev/meldrite/paper/compat/WorldSpawnAbiTransform";
    private static final String SPAWN = "(Lorg/bukkit/Location;Ljava/lang/Class;Ljava/util/function/Consumer;)Lorg/bukkit/entity/Entity;";

    public static <T extends Entity> T spawn(World world, Location location, Class<T> type,
            java.util.function.Consumer<T> consumer) {
        return spawn((RegionAccessor) world, location, type, consumer);
    }

    public static <T extends Entity> T spawn(RegionAccessor world, Location location, Class<T> type,
            java.util.function.Consumer<T> consumer) {
        return world.spawn(location, type, true, consumer == null ? null : consumer::accept);
    }

    private static boolean matches(String owner, String name, String descriptor) {
        return (owner.equals("org/bukkit/World") || owner.equals("org/bukkit/RegionAccessor"))
                && name.equals("spawn") && descriptor.equals(SPAWN);
    }

    private static String bridgeDescriptor(String owner) {
        return "(L" + owner + ";" + SPAWN.substring(1);
    }

    private static Object constant(Object value) {
        if (value instanceof Handle handle && (handle.getTag() == Opcodes.H_INVOKEINTERFACE
                || handle.getTag() == Opcodes.H_INVOKEVIRTUAL)
                && matches(handle.getOwner(), handle.getName(), handle.getDesc())) {
            return new Handle(Opcodes.H_INVOKESTATIC, BRIDGE, "spawn", bridgeDescriptor(handle.getOwner()), false);
        }
        if (value instanceof ConstantDynamic dynamic) {
            var arguments = new Object[dynamic.getBootstrapMethodArgumentCount()];
            boolean changed = false;
            for (int i = 0; i < arguments.length; i++) {
                Object original = dynamic.getBootstrapMethodArgument(i);
                arguments[i] = constant(original);
                changed |= arguments[i] != original;
            }
            if (changed) return new ConstantDynamic(dynamic.getName(), dynamic.getDescriptor(),
                    dynamic.getBootstrapMethod(), arguments);
        }
        return value;
    }

    @Override public byte[] apply(byte[] source, TransformContext context) {
        if (source.length < 10 || source[0] != (byte) 0xCA || source[1] != (byte) 0xFE
                || source[2] != (byte) 0xBA || source[3] != (byte) 0xBE) return source;
        var reader = new ClassReader(source);
        var writer = new ClassWriter(reader, 0);
        boolean[] changed = {false};
        reader.accept(new ClassVisitor(Opcodes.ASM9, writer) {
            @Override public MethodVisitor visitMethod(int access, String name, String descriptor,
                    String signature, String[] exceptions) {
                return new MethodVisitor(Opcodes.ASM9,
                        super.visitMethod(access, name, descriptor, signature, exceptions)) {
                    @Override public void visitMethodInsn(int opcode, String owner, String method,
                            String desc, boolean isInterface) {
                        if ((opcode == Opcodes.INVOKEINTERFACE || opcode == Opcodes.INVOKEVIRTUAL)
                                && matches(owner, method, desc)) {
                            changed[0] = true;
                            super.visitMethodInsn(Opcodes.INVOKESTATIC, BRIDGE, "spawn", bridgeDescriptor(owner), false);
                        } else super.visitMethodInsn(opcode, owner, method, desc, isInterface);
                    }

                    @Override public void visitLdcInsn(Object value) {
                        Object mapped = constant(value);
                        changed[0] |= mapped != value;
                        super.visitLdcInsn(mapped);
                    }

                    @Override public void visitInvokeDynamicInsn(String method, String desc,
                            Handle bootstrap, Object... arguments) {
                        Object[] mapped = arguments.clone();
                        for (int i = 0; i < mapped.length; i++) {
                            mapped[i] = constant(arguments[i]);
                            changed[0] |= mapped[i] != arguments[i];
                        }
                        super.visitInvokeDynamicInsn(method, desc, bootstrap, mapped);
                    }
                };
            }
        }, 0);
        return changed[0] ? writer.toByteArray() : source;
    }
}
