package dev.meldrite.v1201.compat;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

/** Adds the legacy literal constructor to the actual native mutable text class. */
public final class NativeLiteralTextApiTransform {
    private NativeLiteralTextApiTransform() { }

    public static void apply(ClassNode target, String literalContent, String textContent, String style, String emptyStyle) {
        String descriptor = "(Ljava/lang/String;)V";
        if (target.methods.stream().anyMatch(method -> method.name.equals("<init>") && method.desc.equals(descriptor))) return;
        var method = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", descriptor, null, null);
        method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        method.instructions.add(new TypeInsnNode(Opcodes.NEW, literalContent));
        method.instructions.add(new InsnNode(Opcodes.DUP));
        method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        method.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, literalContent, "<init>", "(Ljava/lang/String;)V", false));
        method.instructions.add(new TypeInsnNode(Opcodes.NEW, "java/util/ArrayList"));
        method.instructions.add(new InsnNode(Opcodes.DUP));
        method.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false));
        method.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, style, emptyStyle, "L" + style + ";"));
        method.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, target.name, "<init>",
                "(L" + textContent + ";Ljava/util/List;L" + style + ";)V", false));
        method.instructions.add(new InsnNode(Opcodes.RETURN));
        method.maxStack = 4;
        method.maxLocals = 2;
        target.methods.add(method);
    }
}
