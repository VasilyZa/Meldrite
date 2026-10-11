package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.compat.SpectralArrowNativeAccess;
import dev.meldrite.v1201.hook.PersistentProjectileEntityAccess;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.SpectralArrowEntity;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.bukkit.util.Vector;

/** Typed CraftBukkit facade for the persistent spectral arrow entity. */
public final class CraftSpectralArrow extends CraftProjectile implements org.bukkit.entity.SpectralArrow {
    public CraftSpectralArrow(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public SpectralArrowEntity getHandle() { return (SpectralArrowEntity) super.getHandle(); }
    private PersistentProjectileEntityAccess projectileState() { return (PersistentProjectileEntityAccess) (Object) getHandle(); }
    private SpectralArrowNativeAccess arrowState() { return (SpectralArrowNativeAccess) (Object) getHandle(); }
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
    private static org.bukkit.Sound bukkitSound(SoundEvent sound) { if (sound == null) return null; var id = sound.getId(); return org.bukkit.Registry.SOUNDS.get(new org.bukkit.NamespacedKey(id.getNamespace(), id.getPath())); }
    private static SoundEvent nativeSound(org.bukkit.Sound sound) { java.util.Objects.requireNonNull(sound, "sound"); var key = sound.getKey(); return Registries.SOUND_EVENT.getOrEmpty(new Identifier(key.getNamespace(), key.getKey())).orElseThrow(() -> new IllegalArgumentException("Unknown sound: " + key)); }
    @Override public org.bukkit.Sound getHitSound() { return bukkitSound(projectileState().meldrite$getSound()); }
    @Override public void setHitSound(org.bukkit.Sound sound) { delegate.requireWritable(); projectileState().meldrite$setSound(nativeSound(sound)); }
    @Override public void setNoPhysics(boolean noPhysics) { delegate.requireWritable(); getHandle().setNoClip(noPhysics); }
    @Override public boolean hasNoPhysics() { return getHandle().isNoClip(); }
    @Override public int getGlowingTicks() { return arrowState().meldrite$getDuration(); }
    @Override public void setGlowingTicks(int duration) { if (duration < 0) throw new IllegalArgumentException("duration must be nonnegative"); delegate.requireWritable(); arrowState().meldrite$setDuration(duration); }
}
