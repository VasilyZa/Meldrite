package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.compat.NativeAgeableState;
import net.minecraft.entity.passive.PassiveEntity;

public class CraftAgeable extends CraftCreature implements org.bukkit.entity.Ageable {
    public CraftAgeable(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public PassiveEntity getHandle() { return (PassiveEntity) super.getHandle(); }
    private NativeAgeableState ageState() { return (NativeAgeableState) getHandle(); }
    @Override public int getAge() { return getHandle().getBreedingAge(); }
    @Override public void setAge(int age) { requireWritable(); getHandle().setBreedingAge(age); }
    @Override public void setAgeLock(boolean lock) { requireWritable(); ageState().meldrite$setAgeLocked(lock); }
    @Override public boolean getAgeLock() { return ageState().meldrite$isAgeLocked(); }
    @Override public void setBaby() { if (isAdult()) setAge(-24000); }
    @Override public void setAdult() { if (!isAdult()) setAge(0); }
    @Override public boolean isAdult() { return getAge() >= 0; }
    @Override public boolean canBreed() { return getAge() == 0; }
    @Override public void setBreed(boolean breed) { if (breed) setAge(0); else if (isAdult()) setAge(6000); }
}
