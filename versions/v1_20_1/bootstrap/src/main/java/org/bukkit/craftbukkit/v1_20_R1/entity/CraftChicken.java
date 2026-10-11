package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.passive.ChickenEntity;

public class CraftChicken extends CraftAnimals implements org.bukkit.entity.Chicken {
    public CraftChicken(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public ChickenEntity getHandle() { return (ChickenEntity) super.getHandle(); }
    @Override public boolean isChickenJockey() { return getHandle().hasJockey(); }
    @Override public void setIsChickenJockey(boolean jockey) { requireWritable(); getHandle().setHasJockey(jockey); }
    @Override public int getEggLayTime() { return getHandle().eggLayTime; }
    @Override public void setEggLayTime(int ticks) { requireWritable(); getHandle().eggLayTime = ticks; }
}
