package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.InventoryEventBridge;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerQuickCraftMixin {
    @Inject(method = "onSlotClick", at = @At("HEAD"), cancellable = true)
    private void meldrite$dispatchQuickCraft(int slotIndex, int button, SlotActionType actionType,
            PlayerEntity player, CallbackInfo callback) {
        if (actionType != SlotActionType.QUICK_CRAFT || !(player instanceof ServerPlayerEntity serverPlayer)) return;
        InventoryEventBridge.dispatchQuickCraft((ScreenHandler) (Object) this, slotIndex, button, serverPlayer);
        callback.cancel();
    }
}
