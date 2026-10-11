package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.hook.ArrowEntityAccess;
import dev.meldrite.v1201.hook.PersistentProjectileEntityAccess;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.sound.SoundEvent;

public class CraftArrow extends CraftProjectile implements org.bukkit.entity.Arrow {
    public CraftArrow(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public ArrowEntity getHandle() { return (ArrowEntity) super.getHandle(); }
    private PersistentProjectileEntityAccess projectileState() { return (PersistentProjectileEntityAccess) (Object) getHandle(); }
    private ArrowEntityAccess arrowState() { return (ArrowEntityAccess) (Object) getHandle(); }
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
    private static org.bukkit.Sound bukkitSound(SoundEvent sound) {
        if (sound == null) return null;
        var id = sound.getId(); var result = org.bukkit.Registry.SOUNDS.get(new org.bukkit.NamespacedKey(id.getNamespace(), id.getPath()));
        if (result == null) throw new UnsupportedOperationException("UNREPRESENTABLE_NATIVE_SOUND: " + id);
        return result;
    }
    private static SoundEvent nativeSound(org.bukkit.Sound sound) {
        java.util.Objects.requireNonNull(sound, "sound"); var key = sound.getKey();
        return Registries.SOUND_EVENT.getOrEmpty(new Identifier(key.getNamespace(), key.getKey())).orElseThrow(() -> new IllegalArgumentException("Unknown sound: " + key));
    }
    @Override public org.bukkit.Sound getHitSound() { return bukkitSound(projectileState().meldrite$getSound()); }
    @Override public void setHitSound(org.bukkit.Sound sound) { delegate.requireWritable(); getHandle().setSound(nativeSound(sound)); }
    @Override public void setNoPhysics(boolean noPhysics) { delegate.requireWritable(); getHandle().setNoClip(noPhysics); }
    @Override public boolean hasNoPhysics() { return getHandle().isNoClip(); }
    private static final java.util.Map<org.bukkit.potion.PotionType, String> POTION_NAMES = java.util.Map.ofEntries(
        java.util.Map.entry(org.bukkit.potion.PotionType.UNCRAFTABLE, "empty"), java.util.Map.entry(org.bukkit.potion.PotionType.WATER, "water"), java.util.Map.entry(org.bukkit.potion.PotionType.MUNDANE, "mundane"), java.util.Map.entry(org.bukkit.potion.PotionType.THICK, "thick"), java.util.Map.entry(org.bukkit.potion.PotionType.AWKWARD, "awkward"), java.util.Map.entry(org.bukkit.potion.PotionType.NIGHT_VISION, "night_vision"), java.util.Map.entry(org.bukkit.potion.PotionType.INVISIBILITY, "invisibility"), java.util.Map.entry(org.bukkit.potion.PotionType.JUMP, "leaping"), java.util.Map.entry(org.bukkit.potion.PotionType.FIRE_RESISTANCE, "fire_resistance"), java.util.Map.entry(org.bukkit.potion.PotionType.SPEED, "swiftness"), java.util.Map.entry(org.bukkit.potion.PotionType.SLOWNESS, "slowness"), java.util.Map.entry(org.bukkit.potion.PotionType.WATER_BREATHING, "water_breathing"), java.util.Map.entry(org.bukkit.potion.PotionType.INSTANT_HEAL, "healing"), java.util.Map.entry(org.bukkit.potion.PotionType.INSTANT_DAMAGE, "harming"), java.util.Map.entry(org.bukkit.potion.PotionType.POISON, "poison"), java.util.Map.entry(org.bukkit.potion.PotionType.REGEN, "regeneration"), java.util.Map.entry(org.bukkit.potion.PotionType.STRENGTH, "strength"), java.util.Map.entry(org.bukkit.potion.PotionType.WEAKNESS, "weakness"), java.util.Map.entry(org.bukkit.potion.PotionType.LUCK, "luck"), java.util.Map.entry(org.bukkit.potion.PotionType.TURTLE_MASTER, "turtle_master"), java.util.Map.entry(org.bukkit.potion.PotionType.SLOW_FALLING, "slow_falling"));
    private static String potionPath(org.bukkit.potion.PotionData data) { String name = java.util.Objects.requireNonNull(POTION_NAMES.get(data.getType()), "Unsupported potion type"); return data.isUpgraded() ? "strong_" + name : data.isExtended() ? "long_" + name : name; }
    private static org.bukkit.potion.PotionType potionType(String path) { for (var entry : POTION_NAMES.entrySet()) if (entry.getValue().equals(path)) return entry.getKey(); return org.bukkit.potion.PotionType.UNCRAFTABLE; }
    @Override public void setBasePotionData(org.bukkit.potion.PotionData data) { delegate.requireWritable(); String path = potionPath(data); String base = path.startsWith("strong_") ? path.substring(7) : path.startsWith("long_") ? path.substring(5) : path; arrowState().meldrite$setPotion(Registries.POTION.get(new Identifier("minecraft", path))); arrowState().meldrite$initColor(); }
    @Override public org.bukkit.potion.PotionData getBasePotionData() { String path = Registries.POTION.getId(arrowState().meldrite$getPotion()).getPath(); boolean upgraded = path.startsWith("strong_"); boolean extended = path.startsWith("long_"); String base = upgraded ? path.substring(7) : extended ? path.substring(5) : path; return new org.bukkit.potion.PotionData(potionType(base), extended, upgraded); }
    @Override public org.bukkit.Color getColor() { int color = getHandle().getColor(); return color < 0 ? null : org.bukkit.Color.fromRGB(color); }
    @Override public void setColor(org.bukkit.Color color) { delegate.requireWritable(); arrowState().meldrite$setNativeColor(color == null ? -1 : color.asRGB()); }
    private static org.bukkit.potion.PotionEffect bukkitEffect(StatusEffectInstance effect) { var id = Registries.STATUS_EFFECT.getId(effect.getEffectType()); var type = org.bukkit.potion.PotionEffectType.getByKey(new org.bukkit.NamespacedKey(id.getNamespace(), id.getPath())); if (type == null) throw new UnsupportedOperationException("UNREPRESENTABLE_NATIVE_EFFECT: " + id); return new org.bukkit.potion.PotionEffect(type, effect.getDuration(), effect.getAmplifier(), effect.isAmbient(), effect.shouldShowParticles(), effect.shouldShowIcon()); }
    private static StatusEffectInstance nativeEffect(org.bukkit.potion.PotionEffect effect) { var key = effect.getType().getKey(); var nativeType = Registries.STATUS_EFFECT.get(new Identifier(key.getNamespace(), key.getKey())); return new StatusEffectInstance(nativeType, effect.getDuration(), effect.getAmplifier(), effect.isAmbient(), effect.hasParticles(), effect.hasIcon()); }
    @Override public boolean hasCustomEffects() { return !arrowState().meldrite$getEffects().isEmpty(); }
    @Override public java.util.List<org.bukkit.potion.PotionEffect> getCustomEffects() { return arrowState().meldrite$getEffects().stream().map(CraftArrow::bukkitEffect).toList(); }
    @Override public boolean addCustomEffect(org.bukkit.potion.PotionEffect effect, boolean overwrite) { java.util.Objects.requireNonNull(effect); if (hasCustomEffect(effect.getType()) && !overwrite) return false; removeCustomEffect(effect.getType()); delegate.requireWritable(); arrowState().meldrite$getEffects().add(nativeEffect(effect)); arrowState().meldrite$initColor(); return true; }
    @Override public boolean removeCustomEffect(org.bukkit.potion.PotionEffectType effect) { java.util.Objects.requireNonNull(effect); boolean changed = arrowState().meldrite$getEffects().removeIf(value -> { var id = Registries.STATUS_EFFECT.getId(value.getEffectType()); return effect.getKey().getNamespace().equals(id.getNamespace()) && effect.getKey().getKey().equals(id.getPath()); }); if (changed) { delegate.requireWritable(); arrowState().meldrite$initColor(); } return changed; }
    @Override public boolean hasCustomEffect(org.bukkit.potion.PotionEffectType effect) { return arrowState().meldrite$getEffects().stream().anyMatch(value -> { var id = Registries.STATUS_EFFECT.getId(value.getEffectType()); return effect.getKey().getNamespace().equals(id.getNamespace()) && effect.getKey().getKey().equals(id.getPath()); }); }
    @Override public void clearCustomEffects() { delegate.requireWritable(); if (!arrowState().meldrite$getEffects().isEmpty()) { arrowState().meldrite$getEffects().clear(); arrowState().meldrite$initColor(); } }
}
