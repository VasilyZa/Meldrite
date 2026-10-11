package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.compat.NativeSunburnState;
import net.minecraft.entity.mob.AbstractSkeletonEntity;

public class CraftAbstractSkeleton extends CraftMonster implements org.bukkit.entity.AbstractSkeleton, com.destroystokyo.paper.entity.CraftRangedEntity<AbstractSkeletonEntity> {
    public CraftAbstractSkeleton(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public AbstractSkeletonEntity getHandle() { return (AbstractSkeletonEntity) super.getHandle(); }
    @Override public org.bukkit.entity.Skeleton.SkeletonType getSkeletonType() { return org.bukkit.entity.Skeleton.SkeletonType.NORMAL; }
    @Override public void setSkeletonType(org.bukkit.entity.Skeleton.SkeletonType type) { throw new UnsupportedOperationException("Skeleton subtype is determined by the native entity type"); }
    @Override public boolean shouldBurnInDay() { return ((NativeSunburnState) getHandle()).meldrite$shouldBurnInDay(); }
    @Override public void setShouldBurnInDay(boolean value) { requireWritable(); ((NativeSunburnState) getHandle()).meldrite$setShouldBurnInDay(value); }
}
