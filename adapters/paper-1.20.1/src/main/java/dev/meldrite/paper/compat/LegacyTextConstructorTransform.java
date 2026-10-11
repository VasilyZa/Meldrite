package dev.meldrite.paper.compat;

import dev.meldrite.plugin.spi.ClassTransform;
import dev.meldrite.plugin.spi.TransformContext;
import java.util.ArrayDeque;
import java.util.HashMap;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

/** Maps legacy literal-text construction to the native factory for every internal caller. */
public final class LegacyTextConstructorTransform implements ClassTransform {
    private static final String LEGACY_TEXT = "net/minecraft/network/chat/ChatComponentText";

    @Override public byte[] apply(byte[] source, TransformContext context) {
        if (source.length < 10 || source[0] != (byte) 0xCA || source[1] != (byte) 0xFE
                || source[2] != (byte) 0xBA || source[3] != (byte) 0xBE) return source;
        ClassNode type = new ClassNode();
        new ClassReader(source).accept(type, ClassReader.EXPAND_FRAMES);
        boolean changed = false;
        for (var method : type.methods) {
            var allocations = new ArrayDeque<TypeInsnNode>();
            var uninitialized = new HashMap<LabelNode, AbstractInsnNode>();
            for (AbstractInsnNode instruction : method.instructions) {
                if (instruction instanceof LabelNode label) {
                    AbstractInsnNode next = label.getNext();
                    while (next != null && next.getOpcode() < 0) next = next.getNext();
                    if (next != null && next.getOpcode() == Opcodes.NEW) uninitialized.put(label, next);
                }
            }
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (instruction instanceof TypeInsnNode allocation && allocation.getOpcode() == Opcodes.NEW
                        && allocation.desc.equals(LEGACY_TEXT)) {
                    allocations.push(allocation);
                } else if (instruction instanceof MethodInsnNode call && call.getOpcode() == Opcodes.INVOKESPECIAL
                        && call.owner.equals(LEGACY_TEXT) && call.name.equals("<init>") && !allocations.isEmpty()) {
                    TypeInsnNode allocation = allocations.pop();
                    AbstractInsnNode duplicate = allocation.getNext();
                    while (duplicate != null && duplicate.getOpcode() < 0) duplicate = duplicate.getNext();
                    if (!call.desc.equals("(Ljava/lang/String;)V") || duplicate == null
                            || duplicate.getOpcode() != Opcodes.DUP) continue;
                    boolean storedUninitialized = false;
                    for (AbstractInsnNode entry : method.instructions) {
                        if (entry instanceof FrameNode frame && frame.local != null
                                && frame.local.stream().anyMatch(value -> value instanceof LabelNode label
                                        && uninitialized.get(label) == allocation)) storedUninitialized = true;
                    }
                    if (storedUninitialized) continue;
                    // Argument expressions can branch while the two uninitialized
                    // receiver values are on the stack. Remove them from full frames
                    // as well as from instructions when replacing the constructor.
                    for (AbstractInsnNode entry : method.instructions) {
                        if (entry instanceof FrameNode frame && frame.stack != null) {
                            frame.stack.removeIf(value -> value instanceof LabelNode label
                                    && uninitialized.get(label) == allocation);
                        }
                    }
                    method.instructions.remove(allocation);
                    method.instructions.remove(duplicate);
                    method.instructions.set(call, new MethodInsnNode(Opcodes.INVOKESTATIC,
                            "net/minecraft/network/chat/Component", "literal",
                            "(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;", true));
                    changed = true;
                }
            }
        }
        if (!changed) return source;
        ClassWriter writer = new ClassWriter(0);
        type.accept(writer);
        return writer.toByteArray();
    }
}
