package dev.meldrite.v1201.hook;

import net.minecraft.entity.mob.HoglinEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(HoglinEntity.class)
public interface HoglinEntityPaperAccessMixin {
    @Accessor("timeInOverworld") int meldrite$getTimeInOverworld();
    @Accessor("timeInOverworld") void meldrite$setTimeInOverworld(int value);
    @Accessor("cannotBeHunted") void meldrite$setCannotBeHunted(boolean value);
    @Invoker("isImmuneToZombification") boolean meldrite$isImmuneToZombification();
}
