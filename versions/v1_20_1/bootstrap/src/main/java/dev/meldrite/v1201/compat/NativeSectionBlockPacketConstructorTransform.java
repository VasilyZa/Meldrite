package dev.meldrite.v1201.compat;

import java.util.Objects;
import net.fabricmc.loader.api.MappingResolver;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/** Adds Paper's native section-block packet constructor to the mapped Minecraft packet class. */
public final class NativeSectionBlockPacketConstructorTransform {
    private static final String INTERMEDIARY = "intermediary";
    private static final String PACKET = "net.minecraft.class_2637";
    private static final String SECTION_POS = "net.minecraft.class_4076";
    private static final String BLOCK_STATE = "net.minecraft.class_2680";
    private static final String SHORT_SET = "it/unimi/dsi/fastutil/shorts/ShortSet";
    private static final String SHORT_COLLECTION = "it/unimi/dsi/fastutil/shorts/ShortCollection";

    private NativeSectionBlockPacketConstructorTransform() { }

    public static void apply(ClassNode target, MappingResolver mappings) {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(mappings, "mappings");

        String packet = internalName(mappings.mapClassName(INTERMEDIARY, PACKET));
        if (!target.name.equals(packet)) {
            throw new IllegalArgumentException("Expected ChunkDeltaUpdateS2CPacket, got " + target.name);
        }
        String sectionPos = internalName(mappings.mapClassName(INTERMEDIARY, SECTION_POS));
        String blockState = internalName(mappings.mapClassName(INTERMEDIARY, BLOCK_STATE));
        String sectionDescriptor = "L" + sectionPos + ";";
        String statesDescriptor = "[L" + blockState + ";";
        String descriptor = "(" + sectionDescriptor + "L" + SHORT_SET + ";" + statesDescriptor + ")V";
        if (target.methods.stream().anyMatch(method -> "<init>".equals(method.name) && descriptor.equals(method.desc))) return;

        String sectionField = mappings.mapFieldName(INTERMEDIARY, PACKET, "field_26345", "Lnet/minecraft/class_4076;");
        String positionsField = mappings.mapFieldName(INTERMEDIARY, PACKET, "field_26346", "[S");
        String statesField = mappings.mapFieldName(INTERMEDIARY, PACKET, "field_26347", "[Lnet/minecraft/class_2680;");
        MethodNode constructor = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", descriptor, null, null);
        InsnList instructions = constructor.instructions;
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, packet, sectionField, sectionDescriptor));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
        instructions.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, SHORT_COLLECTION, "toShortArray", "()[S", true));
        instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, packet, positionsField, "[S"));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 3));
        instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, packet, statesField, statesDescriptor));
        instructions.add(new InsnNode(Opcodes.RETURN));
        constructor.maxStack = 2;
        constructor.maxLocals = 4;
        target.methods.add(constructor);
    }

    private static String internalName(String name) { return name.replace('.', '/'); }
}
