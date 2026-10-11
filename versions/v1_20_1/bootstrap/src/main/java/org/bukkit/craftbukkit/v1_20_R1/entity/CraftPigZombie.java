package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;

public class CraftPigZombie extends CraftZombie implements org.bukkit.entity.PigZombie {
    public CraftPigZombie(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public ZombifiedPiglinEntity getHandle() { return (ZombifiedPiglinEntity) super.getHandle(); }
    @Override public int getAnger() { return getHandle().getAngerTime(); }
    @Override public void setAnger(int value) { requireWritable(); getHandle().setAngerTime(value); }
    @Override public void setAngry(boolean value) { setAnger(value ? 400 : 0); }
    @Override public boolean isAngry() { return getAnger() > 0; }
    @Override public boolean isConverting() { return false; }
    @Override public int getConversionTime() { throw new UnsupportedOperationException("Zombified piglins do not convert in water"); }
    @Override public void setConversionTime(int ticks) { throw new UnsupportedOperationException("Zombified piglins do not convert in water"); }
}
