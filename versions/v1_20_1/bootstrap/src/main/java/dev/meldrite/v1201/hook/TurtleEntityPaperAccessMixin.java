package dev.meldrite.v1201.hook;

import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TurtleEntity.class)
public interface TurtleEntityPaperAccessMixin {
    @Invoker("getHomePos") BlockPos meldrite$home();
    @Invoker("setHasEgg") void meldrite$hasEgg(boolean value);
    @Invoker("isLandBound") boolean meldrite$isGoingHome();
}
