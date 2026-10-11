package dev.meldrite.v1201.hook;

import net.minecraft.entity.passive.HorseColor;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.HorseMarking;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(HorseEntity.class)
public interface HorseEntityPaperAccessMixin {
    @Invoker("setHorseVariant") void meldrite$setVariant(HorseColor color, HorseMarking marking);
}
