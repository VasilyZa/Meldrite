package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.mob.WitherSkeletonEntity;

public class CraftWitherSkeleton extends CraftAbstractSkeleton implements org.bukkit.entity.WitherSkeleton {
    public CraftWitherSkeleton(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public WitherSkeletonEntity getHandle() { return (WitherSkeletonEntity) super.getHandle(); }
    @Override public org.bukkit.entity.Skeleton.SkeletonType getSkeletonType() { return org.bukkit.entity.Skeleton.SkeletonType.WITHER; }
}
