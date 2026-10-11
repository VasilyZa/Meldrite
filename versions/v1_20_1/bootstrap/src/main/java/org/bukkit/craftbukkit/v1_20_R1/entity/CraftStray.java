package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.mob.StrayEntity;

public class CraftStray extends CraftAbstractSkeleton implements org.bukkit.entity.Stray {
    public CraftStray(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public StrayEntity getHandle() { return (StrayEntity) super.getHandle(); }
    @Override public org.bukkit.entity.Skeleton.SkeletonType getSkeletonType() { return org.bukkit.entity.Skeleton.SkeletonType.STRAY; }
}
