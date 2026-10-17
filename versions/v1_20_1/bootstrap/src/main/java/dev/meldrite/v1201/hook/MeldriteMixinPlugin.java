package dev.meldrite.v1201.hook;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/** Adds compatibility members directly to the real game class before plugin reflection sees it. */
public final class MeldriteMixinPlugin implements IMixinConfigPlugin {
    private static final org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger("Meldrite");
    private static final String ACCESS = "dev/meldrite/v1201/runtime/ServerSingletonAccess";

    @Override
    public void onLoad(String mixinPackage) {
        dev.meldrite.bootstrap.StartupBanner.print(FabricLoader.getInstance()
                .getModContainer("meldrite").orElseThrow().getMetadata().getVersion().getFriendlyString(), LOGGER::info);
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.endsWith("StateAccess") || mixinClassName.endsWith("Accessor")
                || mixinClassName.endsWith("StateAccessor")) return false;
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        if (mixinClassName.endsWith("ChunkSectionCollisionCacheMixin")) {
            // Mixin rejects newly added public static methods; expose the real decoder
            // through the same native-class forwarding mechanism as other Paper members.
            addStaticForwarder(targetClass, "getKnownBlockInfo", "(IJ)J", targetClass.name,
                    "meldrite$extractKnownBlockInfo");
            addPaperChunkSectionConstructor(targetClass, FabricLoader.getInstance().getMappingResolver());
            return;
        }
        if (mixinClassName.endsWith("PlayerListOrderApiMixin")) {
            dev.meldrite.v1201.compat.NativePlayerListApiTransform.apply(targetClass);
            return;
        }
        if (mixinClassName.endsWith("NativeMaterialMixin")) {
            dev.meldrite.v1201.compat.NativeMaterialApiTransform.apply(targetClass);
            return;
        }
        if (mixinClassName.endsWith("NativeSectionBlockPacketMixin")) {
            dev.meldrite.v1201.compat.NativeSectionBlockPacketConstructorTransform.apply(targetClass,
                    FabricLoader.getInstance().getMappingResolver());
            return;
        }
        if (mixinClassName.endsWith("LegacyPlayerListPacketAliasesMixin")) {
            addLegacyPlayerListArrayConstructor(targetClass);
            addPaperPlayerListConstructors(targetClass, FabricLoader.getInstance().getMappingResolver());
            return;
        }
        if (mixinClassName.endsWith("ServerSingletonMixin")) {
            String descriptor = "()L" + targetClass.name + ";";
            if (targetClass.methods.stream().noneMatch(method -> "getServer".equals(method.name)
                    && descriptor.equals(method.desc))) {
                MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                        "getServer", descriptor, null, null);
                InsnList instructions = method.instructions;
                instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, ACCESS, "getServer",
                        descriptor, false));
                instructions.add(new InsnNode(Opcodes.ARETURN));
                targetClass.methods.add(method);
            }
            return;
        }
        MappingResolver mappings = FabricLoader.getInstance().getMappingResolver();
        if (mixinClassName.endsWith("LegacyLiteralTextAliasesMixin")) {
            String literal = mappings.mapClassName("intermediary", "net.minecraft.class_2585").replace('.', '/');
            String content = mappings.mapClassName("intermediary", "net.minecraft.class_7417").replace('.', '/');
            String style = mappings.mapClassName("intermediary", "net.minecraft.class_2583").replace('.', '/');
            String empty = mappings.mapFieldName("intermediary", "net.minecraft.class_2583", "field_24360", "Lnet/minecraft/class_2583;");
            dev.meldrite.v1201.compat.NativeLiteralTextApiTransform.apply(targetClass, literal, content, style, empty);
            return;
        }
        if (mixinClassName.endsWith("BrewingCustomRecipesMixin")) {
            String stack = "L" + mappings.mapClassName("intermediary", "net.minecraft.class_1799").replace('.', '/') + ";";
            addBrewingMember(targetClass, "isCustomIngredient", "(" + stack + ")Z", "ingredient");
            addBrewingMember(targetClass, "isCustomInput", "(" + stack + ")Z", "input");
            addBrewingMember(targetClass, "addPotionMix", "(Lio/papermc/paper/potion/PotionMix;)V", "add");
            addBrewingMember(targetClass, "removePotionMix", "(Lorg/bukkit/NamespacedKey;)Z", "remove");
            addBrewingMember(targetClass, "reload", "()V", "reset");
            return;
        }
        if (mixinClassName.endsWith("LegacyNbtStringAliasesMixin")) {
            String mappedString = mappings.mapClassName("intermediary", "net.minecraft.class_2519").replace('.', '/');
            String mappedFactory = mappings.mapMethodName("intermediary", "net.minecraft.class_2519", "method_23256",
                    "(Ljava/lang/String;)Lnet/minecraft/class_2519;");
            addStaticForwarder(targetClass, "a", "(Ljava/lang/String;)L" + mappedString + ";", targetClass.name, mappedFactory);
            return;
        }
        if (mixinClassName.endsWith("LegacyStringNbtReaderAliasesMixin")) {
            String mappedCompound = mappings.mapClassName("intermediary", "net.minecraft.class_2487").replace('.', '/');
            String mappedParser = mappings.mapMethodName("intermediary", "net.minecraft.class_2522", "method_10718",
                    "(Ljava/lang/String;)Lnet/minecraft/class_2487;");
            addStaticForwarder(targetClass, "a", "(Ljava/lang/String;)L" + mappedCompound + ";", targetClass.name, mappedParser);
            return;
        }
        if (mixinClassName.endsWith("LegacyNbtIoAliasesMixin")) {
            // The target class is already being transformed in intermediary space. Using named
            // descriptors here leaves a symbolic named class in the injected method and makes
            // the JVM resolve it before Loom can remap the probe or plugin call site.
            String compound = mappings.mapClassName("intermediary", "net.minecraft.class_2487").replace('.', '/');
            String tracker = mappings.mapClassName("intermediary", "net.minecraft.class_2505").replace('.', '/');
            addStaticForwarder(targetClass, "readCompressed",
                    "(Ljava/nio/file/Path;L" + tracker + ";)L" + compound + ";",
                    "dev/meldrite/v1201/compat/NativeNbtIo", "readCompressed");
            return;
        }
        if (mixinClassName.endsWith("LegacyNbtTagSizeTrackerAliasesMixin")) {
            String tracker = mappings.mapClassName("intermediary", "net.minecraft.class_2505").replace('.', '/');
            addStaticForwarder(targetClass, "unlimitedHeap",
                    "()L" + tracker + ";",
                    "dev/meldrite/v1201/compat/NativeNbtIo", "unlimitedHeap");
        }
    }

    /** Paper narrows the biome palette parameter; retain both native palette identities and recount. */
    private static void addPaperChunkSectionConstructor(ClassNode targetClass, MappingResolver mappings) {
        String palette = "L" + mappings.mapClassName("intermediary", "net.minecraft.class_2841").replace('.', '/') + ";";
        String readable = "L" + mappings.mapClassName("intermediary", "net.minecraft.class_7522").replace('.', '/') + ";";
        String descriptor = "(" + palette + palette + ")V";
        if (targetClass.methods.stream().anyMatch(method -> "<init>".equals(method.name) && descriptor.equals(method.desc))) return;
        MethodNode constructor = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", descriptor, null, null);
        constructor.instructions.add(new org.objectweb.asm.tree.VarInsnNode(Opcodes.ALOAD, 0));
        constructor.instructions.add(new org.objectweb.asm.tree.VarInsnNode(Opcodes.ALOAD, 1));
        constructor.instructions.add(new org.objectweb.asm.tree.VarInsnNode(Opcodes.ALOAD, 2));
        constructor.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, targetClass.name, "<init>",
                "(" + palette + readable + ")V", false));
        constructor.instructions.add(new InsnNode(Opcodes.RETURN));
        constructor.maxStack = 3;
        constructor.maxLocals = 3;
        targetClass.methods.add(constructor);
    }

    private static void addStaticForwarder(ClassNode targetClass, String alias, String descriptor,
                                           String owner, String targetMethod) {
        if (targetClass.methods.stream().anyMatch(method -> alias.equals(method.name)
                && descriptor.equals(method.desc))) return;
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                alias, descriptor, null, null);
        int slot = 0;
        for (org.objectweb.asm.Type argument : org.objectweb.asm.Type.getArgumentTypes(descriptor)) {
            method.instructions.add(new org.objectweb.asm.tree.VarInsnNode(argument.getOpcode(Opcodes.ILOAD), slot));
            slot += argument.getSize();
        }
        method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, owner, targetMethod, descriptor, false));
        method.instructions.add(new InsnNode(org.objectweb.asm.Type.getReturnType(descriptor).getOpcode(Opcodes.IRETURN)));
        targetClass.methods.add(method);
    }

    private static void addBrewingMember(ClassNode targetClass, String name, String descriptor, String targetMethod) {
        if (targetClass.methods.stream().anyMatch(method -> name.equals(method.name) && descriptor.equals(method.desc))) return;
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, name, descriptor, null, null);
        if (org.objectweb.asm.Type.getArgumentTypes(descriptor).length > 0) method.instructions.add(new org.objectweb.asm.tree.VarInsnNode(Opcodes.ALOAD, 0));
        method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "dev/meldrite/v1201/compat/NativeBrewing", targetMethod, descriptor, false));
        method.instructions.add(new InsnNode(org.objectweb.asm.Type.getReturnType(descriptor).getOpcode(Opcodes.IRETURN)));
        targetClass.methods.add(method);
    }

    /** Delegates the legacy array shape to the native collection constructor without changing player state. */
    private static void addLegacyPlayerListArrayConstructor(ClassNode targetClass) {
        MappingResolver mappings = FabricLoader.getInstance().getMappingResolver();
        String action = mappings.mapClassName("intermediary", "net.minecraft.class_2703$class_5893").replace('.', '/');
        String player = mappings.mapClassName("intermediary", "net.minecraft.class_3222").replace('.', '/');
        String descriptor = "(L" + action + ";[L" + player + ";)V";
        if (targetClass.methods.stream().noneMatch(method -> "<init>".equals(method.name) && descriptor.equals(method.desc))) {
            MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", descriptor, null, null);
            method.instructions.add(new org.objectweb.asm.tree.VarInsnNode(Opcodes.ALOAD, 0));
            method.instructions.add(new org.objectweb.asm.tree.VarInsnNode(Opcodes.ALOAD, 1));
            method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/util/EnumSet", "of",
                    "(Ljava/lang/Enum;)Ljava/util/EnumSet;", false));
            method.instructions.add(new org.objectweb.asm.tree.VarInsnNode(Opcodes.ALOAD, 2));
            method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/util/Arrays", "asList",
                    "([Ljava/lang/Object;)Ljava/util/List;", false));
            method.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, targetClass.name, "<init>",
                    "(Ljava/util/EnumSet;Ljava/util/Collection;)V", false));
            method.instructions.add(new org.objectweb.asm.tree.InsnNode(Opcodes.RETURN));
            targetClass.methods.add(method);
        }
    }

    /** Adds Paper's native Entry-based packet constructors while retaining its list ownership semantics. */
    private static void addPaperPlayerListConstructors(ClassNode targetClass, MappingResolver mappings) {
        String action = mappings.mapClassName("intermediary", "net.minecraft.class_2703$class_5893").replace('.', '/');
        String entry = mappings.mapClassName("intermediary", "net.minecraft.class_2703$class_2705").replace('.', '/');
        String actionsField = mappings.mapFieldName("intermediary", "net.minecraft.class_2703", "field_40698",
                "Ljava/util/EnumSet;");
        String entriesField = mappings.mapFieldName("intermediary", "net.minecraft.class_2703", "field_12369",
                "Ljava/util/List;");
        String listDescriptor = "(Ljava/util/EnumSet;Ljava/util/List;)V";
        if (targetClass.methods.stream().noneMatch(method -> "<init>".equals(method.name)
                && listDescriptor.equals(method.desc))) {
            String signature = "(Ljava/util/EnumSet<L" + action + ";>;Ljava/util/List<L" + entry + ";>;)V";
            MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", listDescriptor, signature, null);
            InsnList instructions = method.instructions;
            instructions.add(new org.objectweb.asm.tree.VarInsnNode(Opcodes.ALOAD, 0));
            instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false));
            instructions.add(new org.objectweb.asm.tree.VarInsnNode(Opcodes.ALOAD, 0));
            instructions.add(new org.objectweb.asm.tree.VarInsnNode(Opcodes.ALOAD, 1));
            instructions.add(new org.objectweb.asm.tree.FieldInsnNode(Opcodes.PUTFIELD, targetClass.name, actionsField,
                    "Ljava/util/EnumSet;"));
            instructions.add(new org.objectweb.asm.tree.VarInsnNode(Opcodes.ALOAD, 0));
            instructions.add(new org.objectweb.asm.tree.VarInsnNode(Opcodes.ALOAD, 2));
            instructions.add(new org.objectweb.asm.tree.FieldInsnNode(Opcodes.PUTFIELD, targetClass.name, entriesField,
                    "Ljava/util/List;"));
            instructions.add(new InsnNode(Opcodes.RETURN));
            targetClass.methods.add(method);
        }

        String entryDescriptor = "(Ljava/util/EnumSet;L" + entry + ";)V";
        if (targetClass.methods.stream().noneMatch(method -> "<init>".equals(method.name)
                && entryDescriptor.equals(method.desc))) {
            String signature = "(Ljava/util/EnumSet<L" + action + ";>;L" + entry + ";)V";
            MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", entryDescriptor, signature, null);
            InsnList instructions = method.instructions;
            instructions.add(new org.objectweb.asm.tree.VarInsnNode(Opcodes.ALOAD, 0));
            instructions.add(new org.objectweb.asm.tree.VarInsnNode(Opcodes.ALOAD, 1));
            instructions.add(new org.objectweb.asm.tree.VarInsnNode(Opcodes.ALOAD, 2));
            instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/util/List", "of",
                    "(Ljava/lang/Object;)Ljava/util/List;", true));
            instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, targetClass.name, "<init>",
                    listDescriptor, false));
            instructions.add(new InsnNode(Opcodes.RETURN));
            targetClass.methods.add(method);
        }
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
