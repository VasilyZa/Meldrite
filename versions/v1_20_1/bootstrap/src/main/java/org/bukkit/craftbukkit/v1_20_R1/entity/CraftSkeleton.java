package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.hook.SkeletonEntityPaperAccessMixin;
import net.minecraft.entity.mob.SkeletonEntity;

public class CraftSkeleton extends CraftAbstractSkeleton implements org.bukkit.entity.Skeleton {
    public CraftSkeleton(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public SkeletonEntity getHandle() { return (SkeletonEntity) super.getHandle(); }
    private SkeletonEntityPaperAccessMixin conversion() { return (SkeletonEntityPaperAccessMixin) getHandle(); }
    @Override public boolean isConverting() { return getHandle().isConverting(); }
    @Override public int getConversionTime() { if (!isConverting()) throw new IllegalStateException("Entity is not converting"); return conversion().meldrite$getConversionTime(); }
    @Override public void setConversionTime(int ticks) { requireWritable(); if (ticks < 0) { conversion().meldrite$setConversionTime(-1); getHandle().setConverting(false); } else conversion().meldrite$startConversion(ticks); }
    @Override public int inPowderedSnowTime() { return conversion().meldrite$getPowderSnowTicks(); }
}
