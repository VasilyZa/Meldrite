package dev.meldrite.v1201.compat;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/** Supplements the authoritative Player interface without changing plugin bytecode or version detection. */
public final class NativePlayerListApiTransform {
    private NativePlayerListApiTransform() { }

    public static void apply(ClassNode target) {
        add(target, "getPlayerListOrder", "()I");
        add(target, "setPlayerListOrder", "(I)V");
    }

    private static void add(ClassNode target, String name, String descriptor) {
        if (target.methods.stream().noneMatch(method -> method.name.equals(name) && method.desc.equals(descriptor))) {
            target.methods.add(new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT, name, descriptor, null, null));
        }
    }
}
