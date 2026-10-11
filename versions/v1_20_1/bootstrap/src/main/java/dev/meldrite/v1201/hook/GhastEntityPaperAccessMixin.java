package dev.meldrite.v1201.hook;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.GhastEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GhastEntity.class)
public interface GhastEntityPaperAccessMixin {
    @Accessor("fireballStrength") int meldrite$getFireballStrength();
    @Accessor("fireballStrength") void meldrite$setFireballStrength(int value);
}
