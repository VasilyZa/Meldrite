package dev.meldrite.v1201.compat;

import java.util.Set;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

/** Runs before the authoritative Paper API class is defined by Knot. */
public final class NativeMaterialApiTransform {
    private static final String MATERIAL = "org/bukkit/Material";
    private static final String PROJECTION = "dev/meldrite/v1201/compat/NativeMaterialProjection";
    private static final Set<String> PROPERTIES = Set.of("isEmpty", "isBlock", "isItem", "isAir", "isEdible", "isRecord",
            "isSolid", "isTransparent", "isFlammable", "isBurnable", "isFuel", "isOccluding", "hasGravity", "isCollidable",
            "isInteractable", "getHardness", "getBlastResistance", "getSlipperiness", "getCraftingRemainingItem", "getEquipmentSlot",
            "getItemRarity", "getItemAttributes", "getDefaultAttributeModifiers", "getCreativeCategory", "translationKey",
            "getTranslationKey", "getItemTranslationKey", "getBlockTranslationKey", "isEnabledByFeature");

    private NativeMaterialApiTransform() { }

    public static void apply(ClassNode target) {
        if (!MATERIAL.equals(target.name)) throw new IllegalArgumentException("Expected the real Paper Material class");
        if (target.methods.stream().anyMatch(method -> method.name.equals("meldrite$nativeMaterials"))) return;
        for (MethodNode method : target.methods) {
            if ((method.access & Opcodes.ACC_STATIC) == 0 && PROPERTIES.contains(method.name)) injectProperty(method);
            if (method.name.equals("matchMaterial") && method.desc.equals("(Ljava/lang/String;Z)Lorg/bukkit/Material;")) injectMatch(method);
        }
        MethodNode marker = new MethodNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC,
                "meldrite$nativeMaterials", "()V", null, null);
        marker.instructions.add(new InsnNode(Opcodes.RETURN));
        target.methods.add(marker);
    }

    private static void injectProperty(MethodNode method) {
        InsnList code = new InsnList();
        LabelNode original = new LabelNode();
        code.add(new VarInsnNode(Opcodes.ALOAD, 0));
        code.add(new MethodInsnNode(Opcodes.INVOKESTATIC, PROJECTION, "extended", "(Lorg/bukkit/Material;)Z", false));
        code.add(new JumpInsnNode(Opcodes.IFEQ, original));
        code.add(new VarInsnNode(Opcodes.ALOAD, 0));
        code.add(new LdcInsnNode(method.name));
        Type[] arguments = Type.getArgumentTypes(method.desc);
        if (arguments.length == 0) code.add(new InsnNode(Opcodes.ACONST_NULL));
        else if (arguments.length == 1 && arguments[0].getSort() == Type.OBJECT) code.add(new VarInsnNode(Opcodes.ALOAD, 1));
        else throw new IllegalStateException("Unsupported Material property signature: " + method.name + method.desc);
        code.add(new MethodInsnNode(Opcodes.INVOKESTATIC, PROJECTION, "property",
                "(Lorg/bukkit/Material;Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/Object;", false));
        Type result = Type.getReturnType(method.desc);
        if (result.getSort() == Type.BOOLEAN) {
            code.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Boolean"));
            code.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false));
            code.add(new InsnNode(Opcodes.IRETURN));
        } else if (result.getSort() == Type.FLOAT) {
            code.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Float"));
            code.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F", false));
            code.add(new InsnNode(Opcodes.FRETURN));
        } else if (result.getSort() == Type.OBJECT) {
            code.add(new TypeInsnNode(Opcodes.CHECKCAST, result.getInternalName()));
            code.add(new InsnNode(Opcodes.ARETURN));
        } else throw new IllegalStateException("Unsupported Material property result: " + method.name + method.desc);
        code.add(original);
        code.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        method.instructions.insert(code);
        method.maxStack = Math.max(method.maxStack, 3);
    }

    private static void injectMatch(MethodNode method) {
        InsnList code = new InsnList();
        LabelNode original = new LabelNode();
        code.add(new VarInsnNode(Opcodes.ALOAD, 0));
        code.add(new VarInsnNode(Opcodes.ILOAD, 1));
        code.add(new MethodInsnNode(Opcodes.INVOKESTATIC, PROJECTION, "match", "(Ljava/lang/String;Z)Lorg/bukkit/Material;", false));
        code.add(new InsnNode(Opcodes.DUP));
        code.add(new JumpInsnNode(Opcodes.IFNULL, original));
        code.add(new InsnNode(Opcodes.ARETURN));
        code.add(original);
        code.add(new FrameNode(Opcodes.F_SAME1, 0, null, 1, new Object[]{MATERIAL}));
        code.add(new InsnNode(Opcodes.POP));
        method.instructions.insert(code);
        method.maxStack = Math.max(method.maxStack, 2);
    }
}
