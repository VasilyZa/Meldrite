package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeSensorRange;
import net.minecraft.world.event.Vibrations;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Vibrations.VibrationListener.class)
public abstract class VibrationListenerRangeMixin {
    @Shadow @Final private Vibrations receiver;
    @Inject(method = "getRange", at = @At("HEAD"), cancellable = true)
    private void meldrite$range(CallbackInfoReturnable<Integer> callback) {
        if (receiver instanceof NativeSensorRange sensor && sensor.meldrite$getRange() > 0) callback.setReturnValue(sensor.meldrite$getRange());
    }
}
