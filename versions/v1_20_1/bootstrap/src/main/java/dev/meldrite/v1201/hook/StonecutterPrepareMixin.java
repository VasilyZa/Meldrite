package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.InventoryEventBridge;
import net.minecraft.screen.StonecutterScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StonecutterScreenHandler.class)
public abstract class StonecutterPrepareMixin {
    @Inject(method = "populateResult", at = @At("RETURN"))
    private void meldrite$prepare(CallbackInfo ci) { InventoryEventBridge.prepareResult((StonecutterScreenHandler) (Object) this, 1); }
}
