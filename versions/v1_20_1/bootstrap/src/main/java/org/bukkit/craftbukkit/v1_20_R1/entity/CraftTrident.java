package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.compat.TridentNativeAccess;
import dev.meldrite.v1201.hook.PersistentProjectileEntityAccess;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.sound.SoundEvent;

/** CraftBukkit trident projection backed by the live TridentEntity. */
public class CraftTrident extends CraftProjectile implements org.bukkit.entity.Trident {
    public CraftTrident(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public TridentEntity getHandle() { return (TridentEntity) super.getHandle(); }
    private TridentNativeAccess state() { return (TridentNativeAccess) (Object) getHandle(); }
    private PersistentProjectileEntityAccess projectileState() { return (PersistentProjectileEntityAccess) (Object) getHandle(); }
    @Override public int getKnockbackStrength() { return getHandle().getPunch(); }
    @Override public void setKnockbackStrength(int strength) { if (strength < 0) throw new IllegalArgumentException("knockbackStrength must be nonnegative"); delegate.requireWritable(); getHandle().setPunch(strength); }
    @Override public double getDamage() { return getHandle().getDamage(); }
    @Override public void setDamage(double damage) { if (!Double.isFinite(damage) || damage < 0) throw new IllegalArgumentException("damage must be finite and nonnegative"); delegate.requireWritable(); getHandle().setDamage(damage); }
    @Override public int getPierceLevel() { return getHandle().getPierceLevel(); }
    @Override public void setPierceLevel(int level) { if (level < 0 || level > 127) throw new IllegalArgumentException("pierceLevel out of range"); delegate.requireWritable(); getHandle().setPierceLevel((byte) level); }
    @Override public boolean isCritical() { return getHandle().isCritical(); }
    @Override public void setCritical(boolean critical) { delegate.requireWritable(); getHandle().setCritical(critical); }
    @Override public boolean isInBlock() { return projectileState().meldrite$isInGround(); }
    @Override public org.bukkit.block.Block getAttachedBlock() { if (!isInBlock()) return null; var pos = getHandle().getBlockPos(); return getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ()); }
    @Override public PickupStatus getPickupStatus() { return PickupStatus.values()[projectileState().meldrite$getPickupType().ordinal()]; }
    @Override public void setPickupStatus(PickupStatus status) { java.util.Objects.requireNonNull(status, "status"); delegate.requireWritable(); projectileState().meldrite$setPickupType(PersistentProjectileEntity.PickupPermission.fromOrdinal(status.ordinal())); }
    @Override public boolean isShotFromCrossbow() { return getHandle().isShotFromCrossbow(); }
    @Override public void setShotFromCrossbow(boolean shot) { delegate.requireWritable(); getHandle().setShotFromCrossbow(shot); }
    @Override public org.bukkit.inventory.ItemStack getItemStack() { return org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack.asBukkitCopy(projectileState().meldrite$asItemStack()); }
    @Override public void setLifetimeTicks(int ticks) { if (ticks < 0) throw new IllegalArgumentException("ticks must be nonnegative"); delegate.requireWritable(); projectileState().meldrite$setLife(ticks); }
    @Override public int getLifetimeTicks() { return projectileState().meldrite$getLife(); }
    private static org.bukkit.Sound bukkitSound(SoundEvent sound) { if (sound == null) return null; var id = sound.getId(); var result = org.bukkit.Registry.SOUNDS.get(new org.bukkit.NamespacedKey(id.getNamespace(), id.getPath())); if (result == null) throw new UnsupportedOperationException("UNREPRESENTABLE_NATIVE_SOUND: " + id); return result; }
    private static SoundEvent nativeSound(org.bukkit.Sound sound) { java.util.Objects.requireNonNull(sound, "sound"); var key = sound.getKey(); return Registries.SOUND_EVENT.getOrEmpty(new Identifier(key.getNamespace(), key.getKey())).orElseThrow(() -> new IllegalArgumentException("Unknown sound: " + key)); }
    @Override public org.bukkit.Sound getHitSound() { return bukkitSound(projectileState().meldrite$getSound()); }
    @Override public void setHitSound(org.bukkit.Sound sound) { delegate.requireWritable(); getHandle().setSound(nativeSound(sound)); }
    @Override public void setNoPhysics(boolean noPhysics) { delegate.requireWritable(); getHandle().setNoClip(noPhysics); }
    @Override public boolean hasNoPhysics() { return getHandle().isNoClip(); }
    @Override public org.bukkit.inventory.ItemStack getItem() {
        var stack = state().meldrite$getTridentStack();
        return org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack.asBukkitCopy(stack == null ? new net.minecraft.item.ItemStack(net.minecraft.item.Items.TRIDENT) : stack);
    }
    @Override public void setItem(org.bukkit.inventory.ItemStack item) {
        java.util.Objects.requireNonNull(item, "item");
        if (item.getType().isAir()) throw new IllegalArgumentException("Trident item cannot be empty");
        delegate.requireWritable();
        state().meldrite$setTridentStack(org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack.asNMSCopy(item));
    }
    @Override public boolean hasGlint() { return state().meldrite$hasGlint() || getItem().getItemMeta() != null && !getItem().getItemMeta().getEnchants().isEmpty(); }
    @Override public void setGlint(boolean glint) { delegate.requireWritable(); state().meldrite$setGlint(glint); }
    @Override public int getLoyaltyLevel() { return state().meldrite$getLoyalty(); }
    @Override public void setLoyaltyLevel(int level) {
        if (level < 0 || level > 127) throw new IllegalArgumentException("loyalty level outside [0,127]");
        delegate.requireWritable(); state().meldrite$setLoyalty(level);
    }
    @Override public boolean hasDealtDamage() { return state().meldrite$hasDealtDamage(); }
    @Override public void setHasDealtDamage(boolean value) { delegate.requireWritable(); state().meldrite$setDealtDamage(value); }
}
