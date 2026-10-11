package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.InventoryEventBridge;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.server.network.ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerInventoryEventsMixin {
    @Inject(method = "onClickSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/ScreenHandler;onSlotClick(IILnet/minecraft/screen/slot/SlotActionType;Lnet/minecraft/entity/player/PlayerEntity;)V"), cancellable = true)
    private void meldrite$dispatchInventoryClick(ClickSlotC2SPacket packet, CallbackInfo callback) {
        net.minecraft.server.network.ServerPlayNetworkHandler self = (net.minecraft.server.network.ServerPlayNetworkHandler) (Object) this;
        net.minecraft.screen.ScreenHandler handler = self.player.currentScreenHandler;
        boolean cancelled = InventoryEventBridge.dispatchClickEvent(self.player, handler, packet,
                event -> org.bukkit.Bukkit.getPluginManager().callEvent(event));
        if (cancelled || self.player.currentScreenHandler != handler) {
            // The vanilla method disabled remote tracking immediately before the
            // injected call. Restore it before returning from the cancellable hook.
            handler.enableSyncing();
            if (self.player.currentScreenHandler == handler) handler.updateToClient();
            callback.cancel();
        }
    }

    @Inject(method = "onCloseHandledScreen", at = @At("HEAD"))
    private void meldrite$beginPlayerClose(CallbackInfo callback) {
        InventoryEventBridge.pushCloseReason(org.bukkit.event.inventory.InventoryCloseEvent.Reason.PLAYER);
    }

    @Inject(method = "onCloseHandledScreen", at = @At("RETURN"))
    private void meldrite$endPlayerClose(CallbackInfo callback) {
        InventoryEventBridge.popCloseReason();
    }
}
