package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeHorseState;
import net.minecraft.entity.passive.LlamaEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LlamaEntity.class)
public abstract class LlamaEntityPaperStateMixin {
    @Inject(method = "getMaxTemper", at = @At("HEAD"), cancellable = true)
    private void meldrite$temperLimit(CallbackInfoReturnable<Integer> callback) { int value = ((NativeHorseState) this).meldrite$maxDomestication(); if (value > 0) callback.setReturnValue(value); }
}
