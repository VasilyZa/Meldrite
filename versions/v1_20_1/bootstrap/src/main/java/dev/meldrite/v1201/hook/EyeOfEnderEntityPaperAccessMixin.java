package dev.meldrite.v1201.hook;

import net.minecraft.entity.EyeOfEnderEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EyeOfEnderEntity.class)
public interface EyeOfEnderEntityPaperAccessMixin {
    @Accessor("targetX") double meldrite$getTargetX();
    @Accessor("targetX") void meldrite$setTargetX(double value);
    @Accessor("targetY") double meldrite$getTargetY();
    @Accessor("targetY") void meldrite$setTargetY(double value);
    @Accessor("targetZ") double meldrite$getTargetZ();
    @Accessor("targetZ") void meldrite$setTargetZ(double value);
    @Accessor("lifespan") int meldrite$getLifespan();
    @Accessor("lifespan") void meldrite$setLifespan(int value);
    @Accessor("dropsItem") boolean meldrite$getDropsItem();
    @Accessor("dropsItem") void meldrite$setDropsItem(boolean value);
}
