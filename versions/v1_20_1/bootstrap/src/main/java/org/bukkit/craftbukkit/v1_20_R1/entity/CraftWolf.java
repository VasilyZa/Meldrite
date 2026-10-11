package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.passive.WolfEntity;

public class CraftWolf extends CraftTameableAnimal implements org.bukkit.entity.Wolf {
    public CraftWolf(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public WolfEntity getHandle() { return (WolfEntity) super.getHandle(); }
    @Override public boolean isAngry() { return getHandle().hasAngerTime(); }
    @Override public void setAngry(boolean angry) { requireWritable(); if (angry) getHandle().chooseRandomAngerTime(); else getHandle().stopAnger(); }
    @Override public org.bukkit.DyeColor getCollarColor() { return org.bukkit.DyeColor.getByWoolData((byte) getHandle().getCollarColor().getId()); }
    @Override public void setCollarColor(org.bukkit.DyeColor color) { requireWritable(); getHandle().setCollarColor(net.minecraft.util.DyeColor.byId(java.util.Objects.requireNonNull(color, "color").getWoolData())); }
    @Override public boolean isWet() { return getHandle().isFurWet(); }
    @Override public float getTailAngle() { return getHandle().getTailAngle(); }
    @Override public boolean isInterested() { return getHandle().isBegging(); }
    @Override public void setInterested(boolean flag) { requireWritable(); getHandle().setBegging(flag); }
}
