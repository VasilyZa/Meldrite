package dev.meldrite.v1201.hook;

import net.minecraft.entity.mob.SkeletonEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SkeletonEntity.class)
public interface SkeletonEntityPaperAccessMixin {
    @Accessor("conversionTime") int meldrite$getConversionTime();
    @Accessor("conversionTime") void meldrite$setConversionTime(int ticks);
    @Accessor("inPowderSnowTime") int meldrite$getPowderSnowTicks();
    @Invoker("setConversionTime") void meldrite$startConversion(int ticks);
}
