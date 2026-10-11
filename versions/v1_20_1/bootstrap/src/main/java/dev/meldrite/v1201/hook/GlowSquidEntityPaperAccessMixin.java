package dev.meldrite.v1201.hook;

import net.minecraft.entity.passive.GlowSquidEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GlowSquidEntity.class)
public interface GlowSquidEntityPaperAccessMixin {
    @Invoker("setDarkTicksRemaining") void meldrite$setDarkTicksRemaining(int ticks);
}
