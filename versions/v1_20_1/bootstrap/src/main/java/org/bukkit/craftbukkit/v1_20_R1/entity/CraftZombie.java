package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.compat.NativeSunburnState;
import dev.meldrite.v1201.hook.ZombieEntityPaperAccessMixin;
import net.minecraft.entity.mob.ZombieEntity;

public class CraftZombie extends CraftMonster implements org.bukkit.entity.Zombie {
    public CraftZombie(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public ZombieEntity getHandle() { return (ZombieEntity) super.getHandle(); }
    private ZombieEntityPaperAccessMixin zombieState() { return (ZombieEntityPaperAccessMixin) getHandle(); }
    @Override public boolean isBaby() { return getHandle().isBaby(); }
    @Override public void setBaby(boolean value) { requireWritable(); getHandle().setBaby(value); }
    @Override public boolean isVillager() { return getHandle() instanceof net.minecraft.entity.mob.ZombieVillagerEntity; }
    @Override public void setVillager(boolean value) { throw new UnsupportedOperationException("Zombie subtype is determined by the native entity type"); }
    @Override public void setVillagerProfession(org.bukkit.entity.Villager.Profession profession) { throw new UnsupportedOperationException("This zombie has no villager profession"); }
    @Override public org.bukkit.entity.Villager.Profession getVillagerProfession() { return null; }
    @Override public boolean isConverting() { return getHandle().isConvertingInWater(); }
    @Override public int getConversionTime() { if (!isConverting()) throw new IllegalStateException("Entity is not converting"); return zombieState().meldrite$getWaterConversionTicks(); }
    @Override public void setConversionTime(int ticks) { if (ticks < 0) stopDrowning(); else startDrowning(ticks); }
    @Override public boolean isDrowning() { return getHandle().isConvertingInWater(); }
    @Override public void startDrowning(int ticks) { requireWritable(); zombieState().meldrite$startWaterConversion(ticks); }
    @Override public void stopDrowning() { requireWritable(); zombieState().meldrite$setWaterConversionTicks(-1); zombieState().meldrite$setInWaterTime(-1); getHandle().getDataTracker().set(ZombieEntityPaperAccessMixin.meldrite$convertingInWater(), false); }
    @Override public boolean canBreakDoors() { return getHandle().canBreakDoors(); }
    @Override public void setCanBreakDoors(boolean value) { requireWritable(); getHandle().setCanBreakDoors(value); }
    @Override public boolean supportsBreakingDoors() { return zombieState().meldrite$supportsBreakingDoors(); }
    @Override public boolean isArmsRaised() { return getHandle().isAttacking(); }
    @Override public void setArmsRaised(boolean value) { requireWritable(); getHandle().setAttacking(value); }
    @Override public boolean shouldBurnInDay() { return ((NativeSunburnState) getHandle()).meldrite$shouldBurnInDay(); }
    @Override public void setShouldBurnInDay(boolean value) { requireWritable(); ((NativeSunburnState) getHandle()).meldrite$setShouldBurnInDay(value); }
    @Override public int getAge() { return isBaby() ? -1 : 0; }
    @Override public void setAge(int age) { setBaby(age < 0); }
    // Native zombies have binary age, no natural growth and no breeding behavior.
    @Override public void setAgeLock(boolean value) { requireWritable(); }
    @Override public boolean getAgeLock() { return false; }
    @Override public void setBaby() { setBaby(true); }
    @Override public void setAdult() { setBaby(false); }
    @Override public boolean isAdult() { return !isBaby(); }
    @Override public boolean canBreed() { return false; }
    @Override public void setBreed(boolean value) { requireWritable(); }
}
