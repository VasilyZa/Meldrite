package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.passive.GoatEntity;

public class CraftGoat extends CraftAnimals implements org.bukkit.entity.Goat {
    public CraftGoat(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public GoatEntity getHandle() { return (GoatEntity) super.getHandle(); }
    @Override public boolean hasLeftHorn() { return getHandle().hasLeftHorn(); }
    @Override public void setLeftHorn(boolean value) { requireWritable(); getHandle().getDataTracker().set(dev.meldrite.v1201.hook.GoatEntityPaperAccessMixin.meldrite$leftHorn(), value); }
    @Override public boolean hasRightHorn() { return getHandle().hasRightHorn(); }
    @Override public void setRightHorn(boolean value) { requireWritable(); getHandle().getDataTracker().set(dev.meldrite.v1201.hook.GoatEntityPaperAccessMixin.meldrite$rightHorn(), value); }
    @Override public boolean isScreaming() { return getHandle().isScreaming(); }
    @Override public void setScreaming(boolean value) { requireWritable(); getHandle().setScreaming(value); }
    @Override public void ram(org.bukkit.entity.LivingEntity target) {
        requireWritable();
        if (!(target instanceof CraftLivingEntity craft) || craft.getWorld() != getWorld()) throw new IllegalArgumentException("Ram target must be a native living entity in same world");
        var brain = getHandle().getBrain();
        brain.remember(net.minecraft.entity.ai.brain.MemoryModuleType.RAM_TARGET, craft.getHandle().getPos());
        brain.forget(net.minecraft.entity.ai.brain.MemoryModuleType.RAM_COOLDOWN_TICKS);
        brain.forget(net.minecraft.entity.ai.brain.MemoryModuleType.BREED_TARGET);
        brain.forget(net.minecraft.entity.ai.brain.MemoryModuleType.TEMPTING_PLAYER);
        brain.doExclusively(net.minecraft.entity.ai.brain.Activity.RAM);
    }
}
