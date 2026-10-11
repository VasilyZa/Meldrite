package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.mob.ZoglinEntity;

public final class CraftZoglin extends CraftMonster implements org.bukkit.entity.Zoglin {
    public CraftZoglin(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public ZoglinEntity getHandle() { return (ZoglinEntity) super.getHandle(); }
    @Override public boolean isBaby() { return getHandle().isBaby(); }
    @Override public void setBaby(boolean baby) { requireWritable(); getHandle().setBaby(baby); }
    @Override public int getAge() { return isBaby() ? -1 : 0; }
    @Override public void setAge(int age) { setBaby(age < 0); }
    @Override public void setAgeLock(boolean lock) { }
    @Override public boolean getAgeLock() { return false; }
    @Override public void setBaby() { setBaby(true); }
    @Override public void setAdult() { setBaby(false); }
    @Override public boolean isAdult() { return !isBaby(); }
    @Override public boolean canBreed() { return false; }
    @Override public void setBreed(boolean breed) { }
}
