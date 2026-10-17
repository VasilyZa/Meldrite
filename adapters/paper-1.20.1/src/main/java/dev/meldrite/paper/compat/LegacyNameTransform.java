package dev.meldrite.paper.compat;

import dev.meldrite.plugin.spi.ClassTransform;
import java.util.Map;
import dev.meldrite.plugin.spi.TransformContext;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/** Remaps verified native classes and members, including plugin subclasses and reflection. */
public final class LegacyNameTransform implements ClassTransform {
    static {
        dev.meldrite.plugin.classloading.LegacyClassLookup.bindMappings(
                LegacyNameTransform::reflectionClassName, (owner, name, method, parameters, declared) -> {
                    String descriptor = null;
                    if (method) {
                        StringBuilder signature = new StringBuilder("(");
                        if (parameters != null) {
                            for (Class<?> parameter : parameters) signature.append(LegacyMappings.canonicalDescriptor(org.objectweb.asm.Type.getDescriptor(parameter)));
                        }
                        descriptor = signature.append(')').toString();
                    }
                    return LegacyMappings.reflectionMemberNames(owner, name, method, descriptor, declared);
                });
    }

    private static final Map<String, String> NAMES = withCraftAliases(Map.ofEntries(
            Map.entry("io/papermc/paper/plugin/entrypoint/classloader/PaperPluginClassLoader", "dev/meldrite/paper/MeldriteConfiguredClassLoader"),
            Map.entry("io.papermc.paper.plugin.entrypoint.classloader.PaperPluginClassLoader", "dev.meldrite.paper.MeldriteConfiguredClassLoader"),
            Map.entry("net/minecraft/network/chat/ChatComponentText", "net/minecraft/class_5250"),
            Map.entry("net.minecraft.network.chat.ChatComponentText", "net.minecraft.class_5250"),
            Map.entry("net/minecraft/server/MinecraftServer$TickTimes", "dev/meldrite/v1201/compat/LegacyTickTimes"),
            Map.entry("net.minecraft.server.MinecraftServer$TickTimes", "dev.meldrite.v1201.compat.LegacyTickTimes"),
            Map.entry("net/minecraft/server/v1_20_R1/MinecraftServer$TickTimes", "dev/meldrite/v1201/compat/LegacyTickTimes"),
            Map.entry("net.minecraft.server.v1_20_R1.MinecraftServer$TickTimes", "dev.meldrite.v1201.compat.LegacyTickTimes"),
            Map.entry("net/minecraft/server/v1_20_R3/MinecraftServer$TickTimes", "dev/meldrite/v1201/compat/LegacyTickTimes"),
            Map.entry("net.minecraft.server.v1_20_R3.MinecraftServer$TickTimes", "dev.meldrite.v1201.compat.LegacyTickTimes"),
            Map.entry("org/bukkit/craftbukkit/block/data/CraftBlockData", "org/bukkit/craftbukkit/v1_20_R1/block/data/CraftBlockData"),
            Map.entry("org.bukkit.craftbukkit.block.data.CraftBlockData", "org.bukkit.craftbukkit.v1_20_R1.block.data.CraftBlockData"),
            Map.entry("org/bukkit/craftbukkit/v1_20_R3/block/data/CraftBlockData", "org/bukkit/craftbukkit/v1_20_R1/block/data/CraftBlockData"),
            Map.entry("org.bukkit.craftbukkit.v1_20_R3.block.data.CraftBlockData", "org.bukkit.craftbukkit.v1_20_R1.block.data.CraftBlockData"),
            Map.entry("org/bukkit/craftbukkit/block/CraftBlock", "org/bukkit/craftbukkit/v1_20_R1/block/CraftBlock"),
            Map.entry("org.bukkit.craftbukkit.block.CraftBlock", "org.bukkit.craftbukkit.v1_20_R1.block.CraftBlock"),
            Map.entry("org/bukkit/craftbukkit/v1_20_R3/block/CraftBlock", "org/bukkit/craftbukkit/v1_20_R1/block/CraftBlock"),
            Map.entry("org.bukkit.craftbukkit.v1_20_R3.block.CraftBlock", "org.bukkit.craftbukkit.v1_20_R1.block.CraftBlock"),
            Map.entry("org/bukkit/craftbukkit/block/CraftBlockState", "org/bukkit/craftbukkit/v1_20_R1/block/CraftBlockState"),
            Map.entry("org/bukkit/craftbukkit/block/CapturedBlockState", "org/bukkit/craftbukkit/v1_20_R1/block/CapturedBlockState"),
            Map.entry("org.bukkit.craftbukkit.block.CapturedBlockState", "org.bukkit.craftbukkit.v1_20_R1.block.CapturedBlockState"),
            Map.entry("org/bukkit/craftbukkit/v1_20_R3/block/CapturedBlockState", "org/bukkit/craftbukkit/v1_20_R1/block/CapturedBlockState"),
            Map.entry("org.bukkit.craftbukkit.v1_20_R3.block.CapturedBlockState", "org.bukkit.craftbukkit.v1_20_R1.block.CapturedBlockState"),
            Map.entry("org/bukkit/craftbukkit/block/CraftBlockStates", "org/bukkit/craftbukkit/v1_20_R1/block/CraftBlockStates"),
            Map.entry("org.bukkit.craftbukkit.block.CraftBlockStates", "org.bukkit.craftbukkit.v1_20_R1.block.CraftBlockStates"),
            Map.entry("org/bukkit/craftbukkit/v1_20_R3/block/CraftBlockStates", "org/bukkit/craftbukkit/v1_20_R1/block/CraftBlockStates"),
            Map.entry("org.bukkit.craftbukkit.v1_20_R3.block.CraftBlockStates", "org.bukkit.craftbukkit.v1_20_R1.block.CraftBlockStates"),
            Map.entry("org/bukkit/craftbukkit/block/CraftBlockStates$BlockStateFactory", "org/bukkit/craftbukkit/v1_20_R1/block/CraftBlockStates$BlockStateFactory"),
            Map.entry("org.bukkit.craftbukkit.block.CraftBlockStates$BlockStateFactory", "org.bukkit.craftbukkit.v1_20_R1.block.CraftBlockStates$BlockStateFactory"),
            Map.entry("org/bukkit/craftbukkit/v1_20_R3/block/CraftBlockStates$BlockStateFactory", "org/bukkit/craftbukkit/v1_20_R1/block/CraftBlockStates$BlockStateFactory"),
            Map.entry("org.bukkit.craftbukkit.v1_20_R3.block.CraftBlockStates$BlockStateFactory", "org.bukkit.craftbukkit.v1_20_R1.block.CraftBlockStates$BlockStateFactory"),
            Map.entry("org/bukkit/craftbukkit/block/CraftBlockStates$BlockEntityStateFactory", "org/bukkit/craftbukkit/v1_20_R1/block/CraftBlockStates$BlockEntityStateFactory"),
            Map.entry("org.bukkit.craftbukkit.block.CraftBlockStates$BlockEntityStateFactory", "org.bukkit.craftbukkit.v1_20_R1.block.CraftBlockStates$BlockEntityStateFactory"),
            Map.entry("org/bukkit/craftbukkit/v1_20_R3/block/CraftBlockStates$BlockEntityStateFactory", "org/bukkit/craftbukkit/v1_20_R1/block/CraftBlockStates$BlockEntityStateFactory"),
            Map.entry("org.bukkit.craftbukkit.v1_20_R3.block.CraftBlockStates$BlockEntityStateFactory", "org.bukkit.craftbukkit.v1_20_R1.block.CraftBlockStates$BlockEntityStateFactory"),
            Map.entry("org/bukkit/craftbukkit/CraftCrashReport", "org/bukkit/craftbukkit/CraftCrashReport"),
            Map.entry("org.bukkit.craftbukkit.CraftCrashReport", "org.bukkit.craftbukkit.CraftCrashReport"),
            Map.entry("org/bukkit/craftbukkit/CraftEffect", "org/bukkit/craftbukkit/v1_20_R1/CraftEffect"),
            Map.entry("org.bukkit.craftbukkit.CraftEffect", "org.bukkit.craftbukkit.v1_20_R1.CraftEffect"),
            Map.entry("org/bukkit/craftbukkit/inventory/CraftInventoryCustom", "org/bukkit/craftbukkit/v1_20_R1/inventory/CraftInventoryCustom"),
            Map.entry("org.bukkit.craftbukkit.inventory.CraftInventoryCustom", "org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventoryCustom"),
            Map.entry("org/bukkit/craftbukkit/inventory/CraftInventoryDoubleChest", "org/bukkit/craftbukkit/v1_20_R1/inventory/CraftInventoryDoubleChest"),
            Map.entry("org.bukkit.craftbukkit.inventory.CraftInventoryDoubleChest", "org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventoryDoubleChest"),
            Map.entry("org/bukkit/craftbukkit/inventory/CraftBlockInventoryHolder", "org/bukkit/craftbukkit/v1_20_R1/inventory/CraftBlockInventoryHolder"),
            Map.entry("org.bukkit.craftbukkit.inventory.CraftBlockInventoryHolder", "org.bukkit.craftbukkit.v1_20_R1.inventory.CraftBlockInventoryHolder"),
            Map.entry("org/bukkit/craftbukkit/profile/CraftPlayerProfile", "org/bukkit/craftbukkit/profile/CraftPlayerProfile"),
            Map.entry("org.bukkit.craftbukkit.profile.CraftPlayerProfile", "org.bukkit.craftbukkit.profile.CraftPlayerProfile"),
            Map.entry("org/bukkit/craftbukkit/profile/CraftProfileProperty", "org/bukkit/craftbukkit/profile/CraftProfileProperty"),
            Map.entry("org.bukkit.craftbukkit.profile.CraftProfileProperty", "org.bukkit.craftbukkit.profile.CraftProfileProperty"),
            Map.entry("org/bukkit/craftbukkit/block/sign/CraftSignSide", "org/bukkit/craftbukkit/v1_20_R1/block/sign/CraftSignSide"),
            Map.entry("org.bukkit.craftbukkit.block.sign.CraftSignSide", "org.bukkit.craftbukkit.v1_20_R1.block.sign.CraftSignSide"),
            Map.entry("org.bukkit.craftbukkit.block.CraftBlockState", "org.bukkit.craftbukkit.v1_20_R1.block.CraftBlockState"),
            Map.entry("org/bukkit/craftbukkit/v1_20_R3/block/CraftBlockState", "org/bukkit/craftbukkit/v1_20_R1/block/CraftBlockState"),
            Map.entry("org.bukkit.craftbukkit.v1_20_R3.block.CraftBlockState", "org.bukkit.craftbukkit.v1_20_R1.block.CraftBlockState"),
            Map.entry("org/bukkit/craftbukkit/CraftSoundGroup", "org/bukkit/craftbukkit/v1_20_R1/CraftSoundGroup"),
            Map.entry("org.bukkit.craftbukkit.CraftSoundGroup", "org.bukkit.craftbukkit.v1_20_R1.CraftSoundGroup"),
            Map.entry("org/bukkit/craftbukkit/v1_20_R3/CraftSoundGroup", "org/bukkit/craftbukkit/v1_20_R1/CraftSoundGroup"),
            Map.entry("org.bukkit.craftbukkit.v1_20_R3.CraftSoundGroup", "org.bukkit.craftbukkit.v1_20_R1.CraftSoundGroup"),
            Map.entry("org/bukkit/craftbukkit/util/CraftMagicNumbers", "org/bukkit/craftbukkit/v1_20_R1/util/CraftMagicNumbers"),
            Map.entry("org.bukkit.craftbukkit.util.CraftMagicNumbers", "org.bukkit.craftbukkit.v1_20_R1.util.CraftMagicNumbers"),
            Map.entry("org/bukkit/craftbukkit/v1_20_R3/util/CraftMagicNumbers", "org/bukkit/craftbukkit/v1_20_R1/util/CraftMagicNumbers"),
            Map.entry("org.bukkit.craftbukkit.v1_20_R3.util.CraftMagicNumbers", "org.bukkit.craftbukkit.v1_20_R1.util.CraftMagicNumbers"),
            Map.entry("org/bukkit/craftbukkit/util/CraftNamespacedKey", "org/bukkit/craftbukkit/v1_20_R1/util/CraftNamespacedKey"),
            Map.entry("org.bukkit.craftbukkit.util.CraftNamespacedKey", "org.bukkit.craftbukkit.v1_20_R1.util.CraftNamespacedKey"),
            Map.entry("org/bukkit/craftbukkit/v1_20_R3/util/CraftNamespacedKey", "org/bukkit/craftbukkit/v1_20_R1/util/CraftNamespacedKey"),
            Map.entry("org.bukkit.craftbukkit.v1_20_R3.util.CraftNamespacedKey", "org.bukkit.craftbukkit.v1_20_R1.util.CraftNamespacedKey"),
            Map.entry("org/bukkit/craftbukkit/util/CraftChatMessage", "org/bukkit/craftbukkit/v1_20_R1/util/CraftChatMessage"),
            Map.entry("org.bukkit.craftbukkit.util.CraftChatMessage", "org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage"),
            Map.entry("org/bukkit/craftbukkit/v1_20_R3/util/CraftChatMessage", "org/bukkit/craftbukkit/v1_20_R1/util/CraftChatMessage"),
            Map.entry("org.bukkit.craftbukkit.v1_20_R3.util.CraftChatMessage", "org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage"),
            Map.entry("org/bukkit/craftbukkit/util/CraftRayTraceResult", "org/bukkit/craftbukkit/v1_20_R1/util/CraftRayTraceResult"),
            Map.entry("org.bukkit.craftbukkit.util.CraftRayTraceResult", "org.bukkit.craftbukkit.v1_20_R1.util.CraftRayTraceResult"),
            Map.entry("org/bukkit/craftbukkit/v1_20_R3/util/CraftRayTraceResult", "org/bukkit/craftbukkit/v1_20_R1/util/CraftRayTraceResult"),
            Map.entry("org.bukkit.craftbukkit.v1_20_R3.util.CraftRayTraceResult", "org.bukkit.craftbukkit.v1_20_R1.util.CraftRayTraceResult"),
            Map.entry("org/bukkit/craftbukkit/CraftParticle", "org/bukkit/craftbukkit/v1_20_R1/CraftParticle"),
            Map.entry("org.bukkit.craftbukkit.CraftParticle", "org.bukkit.craftbukkit.v1_20_R1.CraftParticle"),
            Map.entry("org/bukkit/craftbukkit/v1_20_R3/CraftParticle", "org/bukkit/craftbukkit/v1_20_R1/CraftParticle"),
            Map.entry("org.bukkit.craftbukkit.v1_20_R3.CraftParticle", "org.bukkit.craftbukkit.v1_20_R1.CraftParticle"),
            Map.entry("org/bukkit/craftbukkit/CraftFeatureFlag", "org/bukkit/craftbukkit/v1_20_R1/CraftFeatureFlag"),
            Map.entry("org.bukkit.craftbukkit.CraftFeatureFlag", "org.bukkit.craftbukkit.v1_20_R1.CraftFeatureFlag"),
            Map.entry("org/bukkit/craftbukkit/v1_20_R3/CraftFeatureFlag", "org/bukkit/craftbukkit/v1_20_R1/CraftFeatureFlag"),
            Map.entry("org.bukkit.craftbukkit.v1_20_R3.CraftFeatureFlag", "org.bukkit.craftbukkit.v1_20_R1.CraftFeatureFlag"),
            Map.entry("org/bukkit/craftbukkit/enchantments/CraftEnchantment", "org/bukkit/craftbukkit/v1_20_R1/enchantments/CraftEnchantment"),
            Map.entry("org.bukkit.craftbukkit.enchantments.CraftEnchantment", "org.bukkit.craftbukkit.v1_20_R1.enchantments.CraftEnchantment"),
            Map.entry("org/bukkit/craftbukkit/v1_20_R3/enchantments/CraftEnchantment", "org/bukkit/craftbukkit/v1_20_R1/enchantments/CraftEnchantment"),
            Map.entry("org.bukkit.craftbukkit.v1_20_R3.enchantments.CraftEnchantment", "org.bukkit.craftbukkit.v1_20_R1.enchantments.CraftEnchantment"),
            Map.entry("net/minecraft/world/level/World", "net/minecraft/class_1937"),
            Map.entry("net.minecraft.world.level.World", "net.minecraft.class_1937"),
            Map.entry("net/minecraft/world/level/Level", "net/minecraft/class_1937"),
            Map.entry("net.minecraft.world.level.Level", "net.minecraft.class_1937"),
            Map.entry("net/minecraft/world/entity/Entity", "net/minecraft/class_1297"),
            Map.entry("net.minecraft.world.entity.Entity", "net.minecraft.class_1297"),
            Map.entry("net/minecraft/world/entity/LivingEntity", "net/minecraft/class_1309"),
            Map.entry("net.minecraft.world.entity.LivingEntity", "net.minecraft.class_1309"),
            Map.entry("net/minecraft/world/entity/EntityLiving", "net/minecraft/class_1309"),
            Map.entry("net.minecraft.world.entity.EntityLiving", "net.minecraft.class_1309"),
            Map.entry("net/minecraft/world/entity/decoration/EntityArmorStand", "net/minecraft/class_1531"),
            Map.entry("net.minecraft.world.entity.decoration.EntityArmorStand", "net.minecraft.class_1531"),
            Map.entry("net/minecraft/world/entity/decoration/ArmorStand", "net/minecraft/class_1531"),
            Map.entry("net.minecraft.world.entity.decoration.ArmorStand", "net.minecraft.class_1531"),
            Map.entry("net/minecraft/world/entity/AreaEffectCloud", "net/minecraft/class_1295"),
            Map.entry("net.minecraft.world.entity.AreaEffectCloud", "net.minecraft.class_1295"),
            Map.entry("net/minecraft/world/entity/EntityAreaEffectCloud", "net/minecraft/class_1295"),
            Map.entry("net.minecraft.world.entity.EntityAreaEffectCloud", "net.minecraft.class_1295"),
            Map.entry("net/minecraft/server/level/ServerLevel", "net/minecraft/class_3218"),
            Map.entry("net.minecraft.server.level.ServerLevel", "net.minecraft.class_3218"),
            Map.entry("net/minecraft/server/level/WorldServer", "net/minecraft/class_3218"),
            Map.entry("net.minecraft.server.level.WorldServer", "net.minecraft.class_3218"),
            Map.entry("net/minecraft/world/phys/Vec3", "net/minecraft/class_243"),
            Map.entry("net.minecraft.world.phys.Vec3", "net.minecraft.class_243"),
            Map.entry("net/minecraft/world/damagesource/DamageSource", "net/minecraft/class_1282"),
            Map.entry("net.minecraft.world.damagesource.DamageSource", "net.minecraft.class_1282"),
            Map.entry("net/minecraft/world/entity/ai/attributes/AttributeBase", "net/minecraft/class_5131"),
            Map.entry("net.minecraft.world.entity.ai.attributes.AttributeBase", "net.minecraft.class_5131"),
            Map.entry("net/minecraft/world/entity/ai/attributes/AttributeModifiable", "net/minecraft/class_1324"),
            Map.entry("net.minecraft.world.entity.ai.attributes.AttributeModifiable", "net.minecraft.class_1324"),
            Map.entry("net/minecraft/world/entity/ai/attributes/GenericAttributes", "net/minecraft/class_5134"),
            Map.entry("net.minecraft.world.entity.ai.attributes.GenericAttributes", "net.minecraft.class_5134"),
            Map.entry("net/minecraft/world/phys/Vec3D", "net/minecraft/class_243"),
            Map.entry("net.minecraft.world.phys.Vec3D", "net.minecraft.class_243"),
            Map.entry("net/minecraft/server/network/PlayerConnection", "net/minecraft/class_3244"),
            Map.entry("net.minecraft.server.network.PlayerConnection", "net.minecraft.class_3244"),
            Map.entry("net/minecraft/server/network/ServerGamePacketListenerImpl", "net/minecraft/class_3244"),
            Map.entry("net.minecraft.server.network.ServerGamePacketListenerImpl", "net.minecraft.class_3244"),
            Map.entry("net/minecraft/network/protocol/Packet", "net/minecraft/class_2596"),
            Map.entry("net.minecraft.network.protocol.Packet", "net.minecraft.class_2596"),
            Map.entry("net/minecraft/network/protocol/game/PacketPlayOutSpawnEntity", "net/minecraft/class_2604"),
            Map.entry("net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity", "net.minecraft.class_2604"),
            Map.entry("net/minecraft/network/protocol/game/ClientboundAddEntityPacket", "net/minecraft/class_2604"),
            Map.entry("net.minecraft.network.protocol.game.ClientboundAddEntityPacket", "net.minecraft.class_2604"),
            Map.entry("net/minecraft/network/protocol/game/PacketPlayOutPlayerInfo", "net/minecraft/class_2703"),
            Map.entry("net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo", "net.minecraft.class_2703"),
            Map.entry("net/minecraft/network/protocol/game/PacketPlayOutPlayerInfo$EnumPlayerInfoAction", "net/minecraft/class_2703$class_5893"),
            Map.entry("net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo.EnumPlayerInfoAction", "net.minecraft.class_2703$class_5893"),
            Map.entry("net/minecraft/network/packet/s2c/play/PlayerListS2CPacket", "net/minecraft/class_2703"),
            Map.entry("net.minecraft.network.packet.s2c.play.PlayerListS2CPacket", "net.minecraft.class_2703"),
            Map.entry("net/minecraft/network/packet/s2c/play/PlayerListS2CPacket$Action", "net/minecraft/class_2703$class_5893"),
            Map.entry("net.minecraft.network.packet.s2c.play.PlayerListS2CPacket$Action", "net.minecraft.class_2703$class_5893"),
            Map.entry("net/minecraft/network/Packet", "net/minecraft/class_2596"),
            Map.entry("net.minecraft.network.Packet", "net.minecraft.class_2596"),
            Map.entry("net/minecraft/network/NetworkManager", "net/minecraft/class_2535"),
            Map.entry("net.minecraft.network.NetworkManager", "net.minecraft.class_2535"),
            Map.entry("net/minecraft/network/Connection", "net/minecraft/class_2535"),
            Map.entry("net.minecraft.network.Connection", "net.minecraft.class_2535"),
            Map.entry("net/minecraft/nbt/NBTBase", "net/minecraft/class_2520"),
            Map.entry("net.minecraft.nbt.NBTBase", "net.minecraft.class_2520"),
            Map.entry("net/minecraft/nbt/NBTTagString", "net/minecraft/class_2519"),
            Map.entry("net.minecraft.nbt.NBTTagString", "net.minecraft.class_2519"),
            Map.entry("net/minecraft/nbt/NBTTagCompound", "net/minecraft/class_2487"),
            Map.entry("net.minecraft.nbt.NBTTagCompound", "net.minecraft.class_2487"),
            Map.entry("net/minecraft/nbt/NBTTagList", "net/minecraft/class_2499"),
            Map.entry("net.minecraft.nbt.NBTTagList", "net.minecraft.class_2499"),
            Map.entry("net/minecraft/nbt/ListTag", "net/minecraft/class_2499"),
            Map.entry("net.minecraft.nbt.ListTag", "net.minecraft.class_2499"),
            Map.entry("net/minecraft/nbt/CompoundTag", "net/minecraft/class_2487"),
            Map.entry("net.minecraft.nbt.CompoundTag", "net.minecraft.class_2487"),
            Map.entry("net/minecraft/nbt/MojangsonParser", "net/minecraft/class_2522"),
            Map.entry("net.minecraft.nbt.MojangsonParser", "net.minecraft.class_2522"),
            Map.entry("net/minecraft/nbt/NbtIo", "net/minecraft/class_2507"),
            Map.entry("net.minecraft.nbt.NbtIo", "net.minecraft.class_2507"),
            Map.entry("net/minecraft/server/dedicated/DedicatedServer", "net/minecraft/server/MinecraftServer"),
            Map.entry("net.minecraft.server.dedicated.DedicatedServer", "net.minecraft.server.MinecraftServer"),
            Map.entry("net/minecraft/core/HolderLookup$a", "net/minecraft/class_7871$class_7872"),
            Map.entry("net.minecraft.core.HolderLookup$a", "net.minecraft.class_7871$class_7872"),
            Map.entry("net/minecraft/world/item/ItemStack", "net/minecraft/class_1799"),
            Map.entry("net.minecraft.world.item.ItemStack", "net.minecraft.class_1799"),
            Map.entry("net/minecraft/world/InteractionHand", "net/minecraft/class_1268"),
            Map.entry("net.minecraft.world.InteractionHand", "net.minecraft.class_1268"),
            Map.entry("net/minecraft/world/EnumHand", "net/minecraft/class_1268"),
            Map.entry("net.minecraft.world.EnumHand", "net.minecraft.class_1268"),
            Map.entry("org/bukkit/craftbukkit/v1_20_R1/inventory/CraftItemStack", "org/bukkit/craftbukkit/v1_20_R1/inventory/CraftItemStack"),
            Map.entry("org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack", "org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack"),
            Map.entry("net/minecraft/server/level/EntityPlayer", "net/minecraft/class_3222"),
            Map.entry("net.minecraft.server.level.EntityPlayer", "net.minecraft.class_3222"),
            Map.entry("net/minecraft/server/level/ServerPlayer", "net/minecraft/class_3222"),
            Map.entry("net.minecraft.server.level.ServerPlayer", "net.minecraft.class_3222"),
            Map.entry("org/bukkit/craftbukkit/v1_20_R3/inventory/CraftItemStack", "org/bukkit/craftbukkit/v1_20_R1/inventory/CraftItemStack"),
            Map.entry("org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack", "org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack"),
            Map.entry("org/bukkit/craftbukkit/CraftServer", "org/bukkit/craftbukkit/v1_20_R1/CraftServer"),
            Map.entry("org.bukkit.craftbukkit.CraftServer", "org.bukkit.craftbukkit.v1_20_R1.CraftServer"),
            Map.entry("org/bukkit/craftbukkit/v1_20_R1/CraftServer", "org/bukkit/craftbukkit/v1_20_R1/CraftServer"),
            Map.entry("org.bukkit.craftbukkit.v1_20_R1.CraftServer", "org.bukkit.craftbukkit.v1_20_R1.CraftServer"),
            Map.entry("org/bukkit/craftbukkit/v1_20_R3/CraftServer", "org/bukkit/craftbukkit/v1_20_R1/CraftServer"),
            Map.entry("org.bukkit.craftbukkit.v1_20_R3.CraftServer", "org.bukkit.craftbukkit.v1_20_R1.CraftServer"),
            Map.entry("org/bukkit/craftbukkit/inventory/CraftItemStack", "org/bukkit/craftbukkit/v1_20_R1/inventory/CraftItemStack"),
            Map.entry("org.bukkit.craftbukkit.inventory.CraftItemStack", "org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack"),
            Map.entry("org/bukkit/craftbukkit/entity/CraftEntity", "org/bukkit/craftbukkit/v1_20_R1/entity/CraftEntity"),
            Map.entry("org.bukkit.craftbukkit.entity.CraftEntity", "org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity"),
            Map.entry("org/bukkit/craftbukkit/entity/CraftPlayer", "org/bukkit/craftbukkit/v1_20_R1/entity/CraftPlayer"),
            Map.entry("org.bukkit.craftbukkit.entity.CraftPlayer", "org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer"),
            Map.entry("org/bukkit/craftbukkit/CraftWorld", "org/bukkit/craftbukkit/v1_20_R1/CraftWorld"),
            Map.entry("org.bukkit.craftbukkit.CraftWorld", "org.bukkit.craftbukkit.v1_20_R1.CraftWorld"),
            Map.entry("org/bukkit/craftbukkit/v1_20_R1/CraftWorld", "org/bukkit/craftbukkit/v1_20_R1/CraftWorld"),
            Map.entry("org.bukkit.craftbukkit.v1_20_R1.CraftWorld", "org.bukkit.craftbukkit.v1_20_R1.CraftWorld"),
            Map.entry("org/bukkit/craftbukkit/v1_20_R3/CraftWorld", "org/bukkit/craftbukkit/v1_20_R1/CraftWorld"),
            Map.entry("org.bukkit.craftbukkit.v1_20_R3.CraftWorld", "org.bukkit.craftbukkit.v1_20_R1.CraftWorld"),
            Map.entry("org/bukkit/craftbukkit/v1_20_R1/entity/CraftPlayer", "org/bukkit/craftbukkit/v1_20_R1/entity/CraftPlayer"),
            Map.entry("org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer", "org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer"),
            Map.entry("org/bukkit/craftbukkit/v1_20_R3/entity/CraftPlayer", "org/bukkit/craftbukkit/v1_20_R1/entity/CraftPlayer"),
            Map.entry("org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer", "org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer"),
            Map.entry("org/bukkit/craftbukkit/v1_20_R1/inventory/CraftMetaSkull", "org/bukkit/craftbukkit/inventory/CraftMetaSkull"),
            Map.entry("org.bukkit.craftbukkit.v1_20_R1.inventory.CraftMetaSkull", "org.bukkit.craftbukkit.inventory.CraftMetaSkull"),
            Map.entry("org/bukkit/craftbukkit/v1_20_R3/inventory/CraftMetaSkull", "org/bukkit/craftbukkit/inventory/CraftMetaSkull"),
            Map.entry("org.bukkit.craftbukkit.v1_20_R3.inventory.CraftMetaSkull", "org.bukkit.craftbukkit.inventory.CraftMetaSkull")));

    private static Map<String, String> withCraftAliases(Map<String, String> names) {
        var result = new java.util.LinkedHashMap<>(names);
        for (String suffix : java.util.List.of(
                "block.data.CraftDirectional", "block.data.CraftOrientable", "block.data.CraftWaterlogged", "block.data.CraftMultipleFacing",
                "block.data.type.CraftStairs", "block.data.type.CraftSlab", "block.data.type.CraftChest", "block.data.type.CraftDoor",
                "block.impl.CraftStairs", "block.impl.CraftStepAbstract", "block.impl.CraftRotatable", "block.impl.CraftChest",
                "block.impl.CraftChestTrapped", "block.impl.CraftDoor", "block.impl.CraftFence", "entity.CraftLivingEntity",
                "persistence.CraftPersistentDataContainer", "persistence.DirtyCraftPersistentDataContainer",
                "persistence.CraftPersistentDataTypeRegistry", "persistence.CraftPersistentDataAdapterContext")) {
            String target = "org.bukkit.craftbukkit.v1_20_R1." + suffix;
            for (String prefix : java.util.List.of("org.bukkit.craftbukkit.", "org.bukkit.craftbukkit.v1_20_R3.")) {
                result.put(prefix + suffix, target);
                result.put((prefix + suffix).replace('.', '/'), target.replace('.', '/'));
            }
        }
        for (String type : java.util.List.of("Item", "Item$SerializableMeta", "Book", "BookSigned", "Potion", "Skull", "EnchantedBook", "SpawnEgg", "Firework", "Charge", "LeatherArmor", "Armor", "ColorableArmor", "Banner", "Map", "Crossbow", "Bundle", "Compass", "EntityTag", "AxolotlBucket", "TropicalFishBucket", "KnowledgeBook", "MusicInstrument", "SuspiciousStew", "ArmorStand", "BlockState")) {
            String suffix = "inventory.CraftMeta" + type;
            String target = "org.bukkit.craftbukkit." + suffix;
            for (String prefix : java.util.List.of("org.bukkit.craftbukkit.v1_20_R1.", "org.bukkit.craftbukkit.v1_20_R3.")) {
                result.put(prefix + suffix, target);
                result.put((prefix + suffix).replace('.', '/'), target.replace('.', '/'));
            }
        }
        for (String prefix : java.util.List.of("org.bukkit.craftbukkit.v1_20_R1.", "org.bukkit.craftbukkit.v1_20_R3.")) {
            result.put(prefix + "inventory.CraftItemFactory", "org.bukkit.craftbukkit.inventory.CraftItemFactory");
            result.put((prefix + "inventory.CraftItemFactory").replace('.', '/'), "org/bukkit/craftbukkit/inventory/CraftItemFactory");
        }
        return Map.copyOf(result);
    }

    public static String reflectionClassName(String name) {
        if (name.startsWith("[") && name.matches("\\[+(?:L[^;]+;|[ZBCSIJFD])")) {
            return new Remapper() {
                @Override public String map(String internal) {
                    String verified = LegacyMappings.className(internal);
                    if (!verified.equals(internal)) return verified;
                    verified = LegacyMappings.className(NAMES.getOrDefault(internal, internal));
                    return verified.equals(internal) ? craftAliasIfPresent(internal) : verified;
                }
            }.mapDesc(name.replace('.', '/')).replace('/', '.');
        }
        String verified = LegacyMappings.reflectionClassName(name);
        if (!verified.equals(name)) return verified;
        verified = LegacyMappings.reflectionClassName(NAMES.getOrDefault(name, name));
        return verified.equals(name) ? reflectionCraftAlias(name) : verified;
    }

    @Override public byte[] apply(byte[] source, TransformContext context) {
        if (source.length < 10 || source[0] != (byte) 0xCA || source[1] != (byte) 0xFE
                || source[2] != (byte) 0xBA || source[3] != (byte) 0xBE) return source;
        String pluginId = context == null ? "" : context.pluginId();
        Map<String, String> names = NAMES;
        ClassReader reader = new ClassReader(source);
        index(reader, pluginId);
        ClassWriter writer = new ClassWriter(0);
        ClassRemapper remapper = new ReflectionMetadataRemapper(writer, new Remapper() {
            @Override public String map(String internalName) {
                String mapped = LegacyMappings.className(internalName);
                if (!mapped.equals(internalName)) return mapped;
                mapped = LegacyMappings.className(names.getOrDefault(internalName, internalName));
                return mapped.equals(internalName) ? craftAliasIfPresent(internalName) : mapped;
            }
            @Override public String mapFieldName(String owner, String name, String descriptor) {
                String mapped = LegacyMappings.fieldName(pluginId, owner, name, descriptor);
                if (!mapped.equals(name) || !names.containsKey(owner)) return mapped;
                return LegacyMappings.fieldName(pluginId, names.get(owner), name, aliasDescriptor(descriptor));
            }
            @Override public String mapMethodName(String owner, String name, String descriptor) {
                String mapped = LegacyMappings.methodName(pluginId, owner, name, descriptor);
                if (!mapped.equals(name) || !names.containsKey(owner)) return mapped;
                return LegacyMappings.methodName(pluginId, names.get(owner), name, aliasDescriptor(descriptor));
            }
            @Override public Object mapValue(Object value) {
                if (value instanceof String text) {
                    // Craft package strings can be inputs to a version relocator.
                    // Resolve them at the Java reflection/native loading boundary,
                    // leaving symbolic JVM type references to map() above.
                    String dotted = text.replace('/', '.');
                    int typeStart = dotted.lastIndexOf('[') + 1;
                    if (typeStart > 0 && typeStart < dotted.length() && dotted.charAt(typeStart) == 'L') typeStart++;
                    if (dotted.startsWith("org.bukkit.craftbukkit.", typeStart)) return text;
                    // A literal may feed a namespace-aware mapping reader. Preserve
                    // names whose Mojang and Spigot meanings differ; direct Java
                    // reflection still goes through the existing lookup bridge.
                    if (LegacyMappings.isAmbiguousReflectionClass(text)) return text;
                    String mapped = LegacyMappings.reflectionClassName(text);
                    if (!mapped.equals(text)) return text.contains("/") ? mapped.replace('.', '/') : mapped;
                    if (names.containsKey(text)) {
                        String alias = names.get(text);
                        String result = LegacyMappings.reflectionClassName(alias);
                        return text.contains("/") ? result.replace('.', '/') : result;
                    }
                }
                return super.mapValue(value);
            }
        });
        reader.accept(new org.objectweb.asm.ClassVisitor(Opcodes.ASM9, remapper) {
            @Override public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                MethodVisitor visitor = super.visitMethod(access, name, descriptor, signature, exceptions);
                return new org.objectweb.asm.MethodVisitor(Opcodes.ASM9, visitor) {
                    @Override public void visitMethodInsn(int opcode, String owner, String method, String desc, boolean isInterface) {
                        if (opcode == Opcodes.INVOKEVIRTUAL
                                && (owner.equals("java/lang/ClassLoader") || owner.equals("java/net/URLClassLoader"))
                                && method.equals("loadClass") && desc.equals("(Ljava/lang/String;)Ljava/lang/Class;")) {
                            super.visitMethodInsn(Opcodes.INVOKESTATIC,
                                    "dev/meldrite/plugin/classloading/LegacyClassLookup", "loadClass",
                                    "(Ljava/lang/ClassLoader;Ljava/lang/String;)Ljava/lang/Class;", false);
                            return;
                        }
                        if (opcode == Opcodes.INVOKEVIRTUAL && owner.equals("java/lang/Class")
                                && ((method.equals("getField") || method.equals("getDeclaredField"))
                                        && desc.equals("(Ljava/lang/String;)Ljava/lang/reflect/Field;")
                                    || (method.equals("getMethod") || method.equals("getDeclaredMethod"))
                                        && desc.equals("(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;"))) {
                            super.visitMethodInsn(Opcodes.INVOKESTATIC,
                                    "dev/meldrite/plugin/classloading/LegacyClassLookup", method,
                                    "(Ljava/lang/Class;" + desc.substring(1), false);
                            return;
                        }
                        if (opcode == Opcodes.INVOKESTATIC && owner.equals("java/lang/Class") && method.equals("forName")
                                && desc.equals("(Ljava/lang/String;)Ljava/lang/Class;")) {
                            super.visitMethodInsn(Opcodes.INVOKESTATIC,
                                    "dev/meldrite/plugin/classloading/LegacyClassLookup", "forName", desc, false);
                            return;
                        }
                        if (opcode == Opcodes.INVOKESTATIC && owner.equals("java/lang/Class") && method.equals("forName")
                                && desc.equals("(Ljava/lang/String;ZLjava/lang/ClassLoader;)Ljava/lang/Class;")) {
                            super.visitMethodInsn(Opcodes.INVOKESTATIC,
                                    "dev/meldrite/plugin/classloading/LegacyClassLookup", "forName", desc, false);
                            return;
                        }
                        super.visitMethodInsn(opcode, owner, method, desc, isInterface);
                    }
                };
            }
        }, 0);
        return writer.toByteArray();
    }

    static void index(ClassReader reader, String pluginId) {
        LegacyMappings.registerHierarchy(pluginId, reader.getClassName(), reader.getSuperName(), reader.getInterfaces(), reader.getAccess());
        LegacyMappings.replaceDeclarations(pluginId, reader.getClassName());
        reader.accept(new org.objectweb.asm.ClassVisitor(Opcodes.ASM9) {
            @Override public org.objectweb.asm.FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                LegacyMappings.registerDeclaration(pluginId, false, reader.getClassName(), name, LegacyMappings.canonicalDescriptor(descriptor), access);
                return null;
            }
            @Override public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                LegacyMappings.registerDeclaration(pluginId, true, reader.getClassName(), name, LegacyMappings.canonicalDescriptor(descriptor), access);
                return null;
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
    }

    /**
     * CraftBukkit keeps most implementation classes under the versioned
     * package.  Older plugins also refer to the unversioned package or to a
     * different 1.20 revision.  Resolve only aliases that are actually
     * present in this distribution; API-only classes such as CraftMetaItem
     * remain in their unversioned adapter package.
     */
    private static String craftAliasIfPresent(String internalName) {
        String candidate = null;
        if (internalName.startsWith("org/bukkit/craftbukkit/v1_20_R3/")) {
            candidate = "org/bukkit/craftbukkit/v1_20_R1/" + internalName.substring("org/bukkit/craftbukkit/v1_20_R3/".length());
        } else if (internalName.startsWith("org/bukkit/craftbukkit/")
                && !internalName.startsWith("org/bukkit/craftbukkit/v1_20_R1/")) {
            candidate = "org/bukkit/craftbukkit/v1_20_R1/" + internalName.substring("org/bukkit/craftbukkit/".length());
        }
        if (candidate == null || candidate.equals(internalName)) return internalName;
        try {
            Class.forName(candidate.replace('/', '.'), false, LegacyNameTransform.class.getClassLoader());
            return candidate;
        } catch (ClassNotFoundException ignored) {
            return internalName;
        }
    }

    /** Normalize compatibility owners before consulting the verified member table. */
    private static String aliasDescriptor(String descriptor) {
        Remapper aliases = new Remapper() {
            @Override public String map(String owner) { return NAMES.getOrDefault(owner, owner); }
        };
        return descriptor.startsWith("(") ? aliases.mapMethodDesc(descriptor) : aliases.mapDesc(descriptor);
    }

    static String reflectionCraftAlias(String name) {
        String internal = name.replace('.', '/');
        String mapped = craftAliasIfPresent(internal);
        return mapped.equals(internal) ? name : (name.indexOf('/') >= 0 ? mapped : mapped.replace('/', '.'));
    }
}
