package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.compat.MinecartNativeAccess;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_20_R1.block.data.CraftBlockData;
import org.bukkit.util.Vector;

public class CraftMinecart extends CraftVehicle implements org.bukkit.entity.Minecart {
    public CraftMinecart(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public AbstractMinecartEntity getHandle() { return (AbstractMinecartEntity) super.getHandle(); }
    private MinecartNativeAccess state() { return (MinecartNativeAccess) (Object) getHandle(); }
    @Override public void setDamage(double damage) { if (!Double.isFinite(damage) || damage < 0) throw new IllegalArgumentException("damage must be finite and nonnegative"); requireWritable(); getHandle().setDamageWobbleStrength((float) damage); }
    @Override public double getDamage() { return getHandle().getDamageWobbleStrength(); }
    @Override public double getMaxSpeed() { return state().meldrite$getMaxSpeed(); }
    @Override public void setMaxSpeed(double speed) { if (!Double.isFinite(speed) || speed < 0) throw new IllegalArgumentException("speed must be finite and nonnegative"); requireWritable(); state().meldrite$setMaxSpeed(speed); }
    @Override public boolean isSlowWhenEmpty() { return state().meldrite$isSlowWhenEmpty(); }
    @Override public void setSlowWhenEmpty(boolean slow) { requireWritable(); state().meldrite$setSlowWhenEmpty(slow); }
    @Override public Vector getFlyingVelocityMod() { return state().meldrite$getFlyingVelocityMod(); }
    @Override public void setFlyingVelocityMod(Vector flying) { java.util.Objects.requireNonNull(flying).checkFinite(); requireWritable(); state().meldrite$setFlyingVelocityMod(flying); }
    @Override public Vector getDerailedVelocityMod() { return state().meldrite$getDerailedVelocityMod(); }
    @Override public void setDerailedVelocityMod(Vector derailed) { java.util.Objects.requireNonNull(derailed).checkFinite(); requireWritable(); state().meldrite$setDerailedVelocityMod(derailed); }
    @Override public org.bukkit.Material getMinecartMaterial() { return org.bukkit.Material.MINECART; }
    @Override public void setDisplayBlockData(BlockData data) { requireWritable(); if (!(data instanceof CraftBlockData craft)) throw new IllegalArgumentException("BlockData has no native state"); getHandle().setCustomBlock(craft.getState()); }
    @Override public BlockData getDisplayBlockData() { return CraftBlockData.fromData(getHandle().getContainedBlock()); }
    @Override public void setDisplayBlock(org.bukkit.material.MaterialData data) { requireWritable(); var block = data.getItemType().createBlockData(); if (!(block instanceof CraftBlockData craft)) throw new IllegalArgumentException("Material has no native state"); getHandle().setCustomBlock(craft.getState()); }
    @Override public org.bukkit.material.MaterialData getDisplayBlock() { return new org.bukkit.material.MaterialData(getDisplayBlockData().getMaterial()); }
    @Override public void setDisplayBlockOffset(int offset) { requireWritable(); getHandle().setCustomBlockOffset(offset); }
    @Override public int getDisplayBlockOffset() { return getHandle().getBlockOffset(); }
}
