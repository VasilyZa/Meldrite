package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.passive.DolphinEntity;
import net.minecraft.util.math.BlockPos;
import org.bukkit.Location;

public class CraftDolphin extends CraftWaterMob implements org.bukkit.entity.Dolphin {
    public CraftDolphin(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public DolphinEntity getHandle() { return (DolphinEntity) super.getHandle(); }
    @Override public int getMoistness() { return getHandle().getMoistness(); }
    @Override public void setMoistness(int moistness) { requireWritable(); getHandle().setMoistness(moistness); }
    @Override public boolean hasFish() { return getHandle().hasFish(); }
    @Override public void setHasFish(boolean value) { requireWritable(); getHandle().setHasFish(value); }
    @Override public Location getTreasureLocation() { var pos = getHandle().getTreasurePos(); return new Location(getWorld(), pos.getX(), pos.getY(), pos.getZ()); }
    @Override public void setTreasureLocation(Location location) { java.util.Objects.requireNonNull(location, "location").checkFinite(); requireWritable(); getHandle().setTreasurePos(BlockPos.ofFloored(location.getX(), location.getY(), location.getZ())); }
}
