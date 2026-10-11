package org.bukkit.craftbukkit.v1_20_R1.entity;
import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.hook.AreaEffectCloudEntityPaperAccessMixin;
import org.bukkit.craftbukkit.v1_20_R1.potion.CraftPotionUtil;
public class CraftAreaEffectCloud extends CraftEntity implements org.bukkit.entity.AreaEffectCloud {
    public CraftAreaEffectCloud(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public net.minecraft.entity.AreaEffectCloudEntity getHandle() { return (net.minecraft.entity.AreaEffectCloudEntity) super.getHandle(); }
    private AreaEffectCloudEntityPaperAccessMixin access() { return (AreaEffectCloudEntityPaperAccessMixin) getHandle(); }
    @Override public int getDuration() { return getHandle().getDuration(); }
    @Override public void setDuration(int value) { delegate.requireWritable(); getHandle().setDuration(value); }
    @Override public int getWaitTime() { return access().meldrite$getWaitTime(); }
    @Override public void setWaitTime(int value) { delegate.requireWritable(); access().meldrite$setWaitTime(value); }
    @Override public int getReapplicationDelay() { return access().meldrite$getReapplicationDelay(); }
    @Override public void setReapplicationDelay(int value) { delegate.requireWritable(); access().meldrite$setReapplicationDelay(value); }
    @Override public int getDurationOnUse() { return access().meldrite$getDurationOnUse(); }
    @Override public void setDurationOnUse(int value) { delegate.requireWritable(); access().meldrite$setDurationOnUse(value); }
    @Override public float getRadius() { return getHandle().getRadius(); }
    @Override public void setRadius(float value) { delegate.requireWritable(); getHandle().setRadius(value); }
    @Override public float getRadiusOnUse() { return access().meldrite$getRadiusOnUse(); }
    @Override public void setRadiusOnUse(float value) { delegate.requireWritable(); access().meldrite$setRadiusOnUse(value); }
    @Override public float getRadiusPerTick() { return access().meldrite$getRadiusGrowth(); }
    @Override public void setRadiusPerTick(float value) { delegate.requireWritable(); access().meldrite$setRadiusGrowth(value); }
    @Override public org.bukkit.Particle getParticle() { return org.bukkit.craftbukkit.v1_20_R1.CraftParticle.toBukkit(getHandle().getParticleType()); }
    @Override public void setParticle(org.bukkit.Particle value) { setParticle(value, null); }
    @Override public <T> void setParticle(org.bukkit.Particle value, T data) { delegate.requireWritable(); getHandle().setParticleType(org.bukkit.craftbukkit.v1_20_R1.CraftParticle.toNMS(value, data)); }
    @Override public org.bukkit.Color getColor() { return org.bukkit.Color.fromRGB(getHandle().getColor()); }
    @Override public void setColor(org.bukkit.Color value) { java.util.Objects.requireNonNull(value, "color"); delegate.requireWritable(); getHandle().setColor(value.asRGB()); }
    @Override public void setBasePotionData(org.bukkit.potion.PotionData value) { var potion = net.minecraft.registry.Registries.POTION.get(new net.minecraft.util.Identifier(CraftPotionUtil.fromBukkit(value))); delegate.requireWritable(); getHandle().setPotion(potion); }
    @Override public org.bukkit.potion.PotionData getBasePotionData() { return CraftPotionUtil.toBukkit(net.minecraft.registry.Registries.POTION.getId(access().meldrite$getPotion()).toString()); }
    @Override public boolean hasCustomEffects() { return !access().meldrite$effects().isEmpty(); }
    @Override public java.util.List<org.bukkit.potion.PotionEffect> getCustomEffects() { return access().meldrite$effects().stream().map(CraftPotionUtil::toBukkit).toList(); }
    @Override public boolean hasCustomEffect(org.bukkit.potion.PotionEffectType type) { java.util.Objects.requireNonNull(type, "type"); return access().meldrite$effects().stream().anyMatch(effect -> CraftPotionUtil.equals(effect.getEffectType(), type)); }
    @Override public boolean addCustomEffect(org.bukkit.potion.PotionEffect effect, boolean overwrite) { var nativeEffect = CraftPotionUtil.fromBukkit(effect); if (hasCustomEffect(effect.getType()) && !overwrite) return false; delegate.requireWritable(); access().meldrite$effects().removeIf(value -> CraftPotionUtil.equals(value.getEffectType(), effect.getType())); getHandle().addEffect(nativeEffect); access().meldrite$updateColor(); return true; }
    @Override public boolean removeCustomEffect(org.bukkit.potion.PotionEffectType type) { java.util.Objects.requireNonNull(type, "type"); delegate.requireWritable(); boolean changed = access().meldrite$effects().removeIf(value -> CraftPotionUtil.equals(value.getEffectType(), type)); if (changed) access().meldrite$updateColor(); return changed; }
    @Override public void clearCustomEffects() { delegate.requireWritable(); access().meldrite$effects().clear(); access().meldrite$updateColor(); }
    @Override public org.bukkit.projectiles.ProjectileSource getSource() { var source = getHandle().getOwner(); return source == null ? null : (org.bukkit.entity.LivingEntity) dev.meldrite.v1201.compat.NativeEntityExtensions.bukkit(source); }
    @Override public void setSource(org.bukkit.projectiles.ProjectileSource value) { delegate.requireWritable(); getHandle().setOwner(value instanceof CraftLivingEntity living ? living.getHandle() : null); }
    @Override public java.util.UUID getOwnerUniqueId() { return access().meldrite$getOwnerUuid(); }
    @Override public void setOwnerUniqueId(java.util.UUID value) { delegate.requireWritable(); getHandle().setOwner(null); access().meldrite$setOwnerUuid(value); }
}
