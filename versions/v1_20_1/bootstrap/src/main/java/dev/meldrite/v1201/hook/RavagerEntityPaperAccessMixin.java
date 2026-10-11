package dev.meldrite.v1201.hook;

import net.minecraft.entity.mob.RavagerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RavagerEntity.class)
public interface RavagerEntityPaperAccessMixin {
    @Accessor("attackTick") void meldrite$setAttackTicks(int ticks);
    @Accessor("stunTick") void meldrite$setStunTicks(int ticks);
    @Accessor("roarTick") void meldrite$setRoarTicks(int ticks);
}
