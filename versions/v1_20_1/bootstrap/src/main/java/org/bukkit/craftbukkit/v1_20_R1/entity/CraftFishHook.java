package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.compat.FishingBobberNativeAccess;
import net.minecraft.entity.projectile.FishingBobberEntity;

/** CraftBukkit fishing hook projection with Paper's tunable hook state. */
public class CraftFishHook extends CraftProjectile implements org.bukkit.entity.FishHook {
    public CraftFishHook(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public FishingBobberEntity getHandle() { return (FishingBobberEntity) super.getHandle(); }
    private FishingBobberNativeAccess state() { return (FishingBobberNativeAccess) (Object) getHandle(); }
    private static void range(boolean ok, String message) { if (!ok) throw new IllegalArgumentException(message); }
    @Override public int getMinWaitTime() { return state().meldrite$getMinWaitTime(); }
    @Override public void setMinWaitTime(int value) { range(value >= 0 && value <= getMaxWaitTime(), "min wait outside range"); delegate.requireWritable(); state().meldrite$setMinWaitTime(value); }
    @Override public int getMaxWaitTime() { return state().meldrite$getMaxWaitTime(); }
    @Override public void setMaxWaitTime(int value) { range(value >= getMinWaitTime(), "max wait below min wait"); delegate.requireWritable(); state().meldrite$setMaxWaitTime(value); }
    @Override public void setWaitTime(int min, int max) { range(min >= 0 && max >= min, "invalid wait range"); delegate.requireWritable(); state().meldrite$setMinWaitTime(min); state().meldrite$setMaxWaitTime(max); }
    @Override public int getMinLureTime() { return state().meldrite$getMinLureTime(); }
    @Override public void setMinLureTime(int value) { range(value >= 0 && value <= getMaxLureTime(), "min lure outside range"); delegate.requireWritable(); state().meldrite$setMinLureTime(value); }
    @Override public int getMaxLureTime() { return state().meldrite$getMaxLureTime(); }
    @Override public void setMaxLureTime(int value) { range(value >= getMinLureTime(), "max lure below min lure"); delegate.requireWritable(); state().meldrite$setMaxLureTime(value); }
    @Override public void setLureTime(int min, int max) { range(min >= 0 && max >= min, "invalid lure range"); delegate.requireWritable(); state().meldrite$setMinLureTime(min); state().meldrite$setMaxLureTime(max); }
    @Override public float getMinLureAngle() { return state().meldrite$getMinLureAngle(); }
    @Override public void setMinLureAngle(float value) { range(Float.isFinite(value) && value <= getMaxLureAngle(), "min lure angle above max"); delegate.requireWritable(); state().meldrite$setMinLureAngle(value); }
    @Override public float getMaxLureAngle() { return state().meldrite$getMaxLureAngle(); }
    @Override public void setMaxLureAngle(float value) { range(Float.isFinite(value) && value >= getMinLureAngle(), "max lure angle below min"); delegate.requireWritable(); state().meldrite$setMaxLureAngle(value); }
    @Override public void setLureAngle(float min, float max) { range(Float.isFinite(min) && Float.isFinite(max) && min <= max, "invalid lure angle range"); delegate.requireWritable(); state().meldrite$setMinLureAngle(min); state().meldrite$setMaxLureAngle(max); }
    @Override public boolean getApplyLure() { return state().meldrite$getApplyLure(); }
    @Override public void setApplyLure(boolean value) { delegate.requireWritable(); state().meldrite$setApplyLure(value); }
    @Override public double getBiteChance() {
        double value = state().meldrite$getBiteChance();
        if (value >= 0) return value;
        var pos = getHandle().getBlockPos().up();
        return getHandle().getWorld().isRaining() && getHandle().getWorld().hasRain(pos) ? 1.0 / 300.0 : 1.0 / 500.0;
    }
    @Override public void setBiteChance(double value) { range(Double.isFinite(value) && value >= 0 && value <= 1, "bite chance outside [0,1]"); delegate.requireWritable(); state().meldrite$setBiteChance(value); }
    @Override public boolean isInOpenWater() { return getHandle().isInOpenWater(); }
    @Override public org.bukkit.entity.Entity getHookedEntity() { var entity = state().meldrite$getHookedEntity(); return entity == null || entity.isRemoved() ? null : dev.meldrite.v1201.compat.NativeEntityExtensions.bukkit(entity); }
    @Override public void setHookedEntity(org.bukkit.entity.Entity entity) {
        delegate.requireWritable();
        if (entity != null && !(entity instanceof CraftEntity)) throw new IllegalArgumentException("FOREIGN_ENTITY_FACADE");
        state().meldrite$setHookedEntity(entity == null ? null : ((CraftEntity) entity).getHandle());
    }
    @Override public boolean pullHookedEntity() { var entity = state().meldrite$getHookedEntity(); if (entity == null) return false; state().meldrite$pullHookedEntity(entity); return true; }
    @Override public boolean isSkyInfluenced() { return state().meldrite$isSkyInfluenced(); }
    @Override public void setSkyInfluenced(boolean value) { delegate.requireWritable(); state().meldrite$setSkyInfluenced(value); }
    @Override public boolean isRainInfluenced() { return state().meldrite$isRainInfluenced(); }
    @Override public void setRainInfluenced(boolean value) { delegate.requireWritable(); state().meldrite$setRainInfluenced(value); }
    @Override public HookState getState() {
        if (state().meldrite$getHookedEntity() != null) return HookState.HOOKED_ENTITY;
        return isInOpenWater() ? HookState.BOBBING : HookState.UNHOOKED;
    }
    @Override public int getWaitTime() { return state().meldrite$getWaitTime(); }
    @Override public void setWaitTime(int value) { range(value >= 0, "wait time must be nonnegative"); delegate.requireWritable(); state().meldrite$setWaitTime(value); }
}
