package dev.meldrite.v1201.hook;

import net.minecraft.entity.mob.VexEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(VexEntity.class)
public interface VexEntityPaperAccessMixin {
    @Accessor("alive") boolean meldrite$hasLimitedLife();
    @Accessor("alive") void meldrite$setLimitedLife(boolean value);
    @Accessor("lifeTicks") int meldrite$getLifeTicks();
    @Accessor("lifeTicks") void meldrite$setLifeTicks(int ticks);
}
