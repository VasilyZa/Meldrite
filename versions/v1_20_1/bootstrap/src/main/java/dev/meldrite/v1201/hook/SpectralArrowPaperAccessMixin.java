package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.SpectralArrowNativeAccess;
import net.minecraft.entity.projectile.SpectralArrowEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SpectralArrowEntity.class)
public interface SpectralArrowPaperAccessMixin extends SpectralArrowNativeAccess {
    @Override @Accessor("duration") int meldrite$getDuration();
    @Override @Accessor("duration") void meldrite$setDuration(int value);
}
