package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.hook.SkeletonHorseEntityPaperAccessMixin;
import net.minecraft.entity.mob.SkeletonHorseEntity;

public class CraftSkeletonHorse extends CraftAbstractHorse implements org.bukkit.entity.SkeletonHorse {
    public CraftSkeletonHorse(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public SkeletonHorseEntity getHandle() { return (SkeletonHorseEntity) super.getHandle(); }
    @Override public org.bukkit.entity.Horse.Variant getVariant() { return org.bukkit.entity.Horse.Variant.SKELETON_HORSE; }
    @Override public boolean isTrapped() { return getHandle().isTrapped(); }
    @Override public void setTrapped(boolean value) { requireWritable(); getHandle().setTrapped(value); }
    @Override public int getTrapTime() { return ((SkeletonHorseEntityPaperAccessMixin) getHandle()).meldrite$getTrapTime(); }
    @Override public void setTrapTime(int value) { requireWritable(); ((SkeletonHorseEntityPaperAccessMixin) getHandle()).meldrite$setTrapTime(value); }
    @Override public boolean isTrap() { return isTrapped(); }
    @Override public void setTrap(boolean value) { setTrapped(value); }
}
