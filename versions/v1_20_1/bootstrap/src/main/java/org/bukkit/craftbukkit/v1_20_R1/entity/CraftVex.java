package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.compat.NativeEntityExtensions;
import dev.meldrite.v1201.hook.VexEntityPaperAccessMixin;
import net.minecraft.entity.mob.VexEntity;
import net.minecraft.util.math.BlockPos;
import org.bukkit.Location;
import org.bukkit.entity.Mob;

public class CraftVex extends CraftMonster implements org.bukkit.entity.Vex {
    public CraftVex(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public VexEntity getHandle() { return (VexEntity) super.getHandle(); }
    private VexEntityPaperAccessMixin vexState() { return (VexEntityPaperAccessMixin) getHandle(); }
    @Override public boolean isCharging() { return getHandle().isCharging(); }
    @Override public void setCharging(boolean value) { requireWritable(); getHandle().setCharging(value); }
    @Override public Location getBound() { var p = getHandle().getBounds(); return p == null ? null : new Location(getWorld(), p.getX(), p.getY(), p.getZ()); }
    @Override public void setBound(Location location) { if (location != null) { location.checkFinite(); if (!getWorld().equals(location.getWorld())) throw new IllegalArgumentException("Bound must be in the same world"); } requireWritable(); getHandle().setBounds(location == null ? null : BlockPos.ofFloored(location.getX(), location.getY(), location.getZ())); }
    @Override public int getLifeTicks() { return vexState().meldrite$getLifeTicks(); }
    @Override public void setLifeTicks(int ticks) { requireWritable(); getHandle().setLifeTicks(ticks); if (ticks < 0) vexState().meldrite$setLimitedLife(false); }
    @Override public boolean hasLimitedLife() { return vexState().meldrite$hasLimitedLife(); }
    @Override public boolean hasLimitedLifetime() { return hasLimitedLife(); }
    @Override public void setLimitedLifetime(boolean value) { requireWritable(); vexState().meldrite$setLimitedLife(value); }
    @Override public int getLimitedLifetimeTicks() { return getLifeTicks(); }
    @Override public void setLimitedLifetimeTicks(int ticks) { requireWritable(); vexState().meldrite$setLifeTicks(ticks); }
    @Override public Mob getSummoner() { var owner = getHandle().getOwner(); return owner == null ? null : (Mob) NativeEntityExtensions.bukkit(owner); }
    @Override public void setSummoner(Mob summoner) { if (summoner != null && !(summoner instanceof CraftMob)) throw new IllegalArgumentException("Summoner must be native-backed"); requireWritable(); getHandle().setOwner(summoner == null ? null : ((CraftMob) summoner).getHandle()); }
}
