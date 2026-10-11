package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.InventoryEventBridge;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerInventoryCloseMixin {
    @Inject(method = "onClosed", at = @At("HEAD"))
    private void meldrite$dispatchInventoryClose(PlayerEntity player, CallbackInfo callback) {
        InventoryEventBridge.fireCloseEvent(player, (ScreenHandler) (Object) this);
    }
}
