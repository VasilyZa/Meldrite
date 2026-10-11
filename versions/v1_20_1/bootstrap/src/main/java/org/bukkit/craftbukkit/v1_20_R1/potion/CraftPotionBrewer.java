package org.bukkit.craftbukkit.v1_20_R1.potion;

import java.util.Collection;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.bukkit.NamespacedKey;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class CraftPotionBrewer implements org.bukkit.potion.PotionBrewer {
    @Override public Collection<PotionEffect> getEffects(PotionType type, boolean upgraded, boolean extended) {
        return Registries.POTION.get(new Identifier(CraftPotionUtil.fromBukkit(new PotionData(type, extended, upgraded))))
                .getEffects().stream().map(CraftPotionUtil::toBukkit).toList();
    }
    @Override public Collection<PotionEffect> getEffectsFromDamage(int damage) {
        var old = new net.minecraft.nbt.NbtCompound();
        old.putInt("id", 373); old.putShort("Damage", (short) damage); old.putByte("Count", (byte) 1);
        // ItemPotionFix only upgrades potion damage when the tag field exists.
        old.put("tag", new net.minecraft.nbt.NbtCompound());
        var fixed = net.minecraft.datafixer.Schemas.getFixer().update(net.minecraft.datafixer.TypeReferences.ITEM_STACK,
                new com.mojang.serialization.Dynamic<net.minecraft.nbt.NbtElement>(net.minecraft.nbt.NbtOps.INSTANCE, old), -1,
                net.minecraft.SharedConstants.getGameVersion().getSaveVersion().getId());
        var stack = net.minecraft.item.ItemStack.fromNbt((net.minecraft.nbt.NbtCompound) fixed.getValue());
        return net.minecraft.potion.PotionUtil.getPotionEffects(stack).stream().map(CraftPotionUtil::toBukkit).toList();
    }
    @Override public PotionEffect createEffect(PotionEffectType type, int duration, int amplifier) {
        java.util.Objects.requireNonNull(type); return new PotionEffect(type, type.isInstant() ? 1 : (int) (duration * type.getDurationModifier()), amplifier);
    }
    @Override public void addPotionMix(io.papermc.paper.potion.PotionMix mix) { dev.meldrite.v1201.compat.NativeBrewing.add(mix); }
    @Override public void removePotionMix(NamespacedKey key) { dev.meldrite.v1201.compat.NativeBrewing.remove(key); }
    @Override public void resetPotionMixes() { dev.meldrite.v1201.compat.NativeBrewing.reset(); }
}
