package dev.meldrite.v1201.hook;

import net.minecraft.entity.mob.SkeletonHorseEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SkeletonHorseEntity.class)
public interface SkeletonHorseEntityPaperAccessMixin {
    @Accessor("trapTime") int meldrite$getTrapTime();
    @Accessor("trapTime") void meldrite$setTrapTime(int value);
}
