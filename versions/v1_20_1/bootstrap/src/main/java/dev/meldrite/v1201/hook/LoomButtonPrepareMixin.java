package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.InventoryEventBridge;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.LoomScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LoomScreenHandler.class)
public abstract class LoomButtonPrepareMixin {
    @Inject(method = "onButtonClick", at = @At("RETURN"))
    private void meldrite$prepare(PlayerEntity player, int button, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) InventoryEventBridge.prepareResult(player, (LoomScreenHandler) (Object) this, 3);
    }
}
