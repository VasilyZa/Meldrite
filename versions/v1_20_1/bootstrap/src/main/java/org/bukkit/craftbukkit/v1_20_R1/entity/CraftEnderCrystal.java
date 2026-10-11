package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.decoration.EndCrystalEntity;

public final class CraftEnderCrystal extends CraftEntity implements org.bukkit.entity.EnderCrystal {
    public CraftEnderCrystal(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public EndCrystalEntity getHandle() { return (EndCrystalEntity) super.getHandle(); }
    @Override public boolean isShowingBottom() { return getHandle().shouldShowBottom(); }
    @Override public void setShowingBottom(boolean showing) { delegate.requireWritable(); getHandle().setShowBottom(showing); }
    @Override public org.bukkit.Location getBeamTarget() { var pos = getHandle().getBeamTarget(); return pos == null ? null : new org.bukkit.Location(getWorld(), pos.getX(), pos.getY(), pos.getZ()); }
    @Override public void setBeamTarget(org.bukkit.Location location) { delegate.requireWritable(); if (location == null) { getHandle().setBeamTarget(null); return; } location.checkFinite(); if (location.getWorld() != getWorld()) throw new IllegalArgumentException("BEAM_TARGET_DIFFERENT_WORLD"); getHandle().setBeamTarget(new net.minecraft.util.math.BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ())); }
}
