package dev.meldrite.v1201.hook;

import net.minecraft.entity.passive.PandaEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PandaEntity.class)
public interface PandaEntityPaperAccessMixin {
    @Invoker("getEatingTicks") int meldrite$eatingTicks();
    @Invoker("setEatingTicks") void meldrite$eatingTicks(int ticks);
}
