package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.passive.BeeEntity;

public class CraftBee extends CraftAnimals implements org.bukkit.entity.Bee {
    public CraftBee(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public BeeEntity getHandle() { return (BeeEntity) super.getHandle(); }
    private dev.meldrite.v1201.hook.BeeEntityPaperAccessMixin beeState() { return (dev.meldrite.v1201.hook.BeeEntityPaperAccessMixin) getHandle(); }
    private dev.meldrite.v1201.compat.NativeBeeState paperState() { return (dev.meldrite.v1201.compat.NativeBeeState) getHandle(); }
    private org.bukkit.Location location(net.minecraft.util.math.BlockPos pos) { return pos == null ? null : new org.bukkit.Location(getWorld(), pos.getX(), pos.getY(), pos.getZ()); }
    private net.minecraft.util.math.BlockPos position(org.bukkit.Location location) {
        if (location == null) return null;
        location.checkFinite(); if (location.getWorld() != getWorld()) throw new IllegalArgumentException("Bee location must be in same world");
        return net.minecraft.util.math.BlockPos.ofFloored(location.getX(), location.getY(), location.getZ());
    }
    @Override public org.bukkit.Location getHive() { return location(getHandle().getHivePos()); }
    @Override public void setHive(org.bukkit.Location location) { requireWritable(); beeState().meldrite$hive(position(location)); }
    @Override public org.bukkit.Location getFlower() { return location(getHandle().getFlowerPos()); }
    @Override public void setFlower(org.bukkit.Location location) { requireWritable(); getHandle().setFlowerPos(position(location)); }
    @Override public boolean hasNectar() { return getHandle().hasNectar(); }
    @Override public void setHasNectar(boolean flag) { requireWritable(); beeState().meldrite$nectar(flag); }
    @Override public boolean hasStung() { return getHandle().hasStung(); }
    @Override public void setHasStung(boolean flag) { requireWritable(); beeState().meldrite$stung(flag); }
    @Override public int getAnger() { return getHandle().getAngerTime(); }
    @Override public void setAnger(int ticks) { requireWritable(); getHandle().setAngerTime(ticks); }
    @Override public int getCannotEnterHiveTicks() { return beeState().meldrite$hiveDelay(); }
    @Override public void setCannotEnterHiveTicks(int ticks) { requireWritable(); getHandle().setCannotEnterHiveTicks(ticks); }
    @Override public void setRollingOverride(net.kyori.adventure.util.TriState value) { requireWritable(); paperState().meldrite$rollingOverride(value); }
    @Override public net.kyori.adventure.util.TriState getRollingOverride() { return paperState().meldrite$rollingOverride(); }
    @Override public boolean isRolling() { return getRollingOverride().toBooleanOrElse(beeState().meldrite$isRolling()); }
    @Override public int getCropsGrownSincePollination() { return beeState().meldrite$crops(); }
    @Override public void setCropsGrownSincePollination(int crops) { requireWritable(); beeState().meldrite$crops(crops); }
    @Override public int getTicksSincePollination() { return beeState().meldrite$pollinationTicks(); }
    @Override public void setTicksSincePollination(int ticks) { requireWritable(); beeState().meldrite$pollinationTicks(ticks); }
}
