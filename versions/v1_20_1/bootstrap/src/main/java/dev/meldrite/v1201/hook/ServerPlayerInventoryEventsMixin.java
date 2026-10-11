package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.InventoryEventBridge;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerInventoryEventsMixin {
    @Inject(method = "openHandledScreen", at = @At("HEAD"))
    private void meldrite$beginOpen(CallbackInfoReturnable<java.util.OptionalInt> callback) {
        InventoryEventBridge.pushCloseReason(org.bukkit.event.inventory.InventoryCloseEvent.Reason.OPEN_NEW);
    }

    @Inject(method = "openHandledScreen", at = @At("RETURN"))
    private void meldrite$endOpen(CallbackInfoReturnable<java.util.OptionalInt> callback) {
        InventoryEventBridge.popCloseReason();
    }

    @ModifyVariable(method = "openHandledScreen", at = @At("HEAD"), argsOnly = true)
    private NamedScreenHandlerFactory meldrite$dispatchInventoryOpen(NamedScreenHandlerFactory factory) {
        return factory == null ? null : InventoryEventBridge.wrapOpenFactory((ServerPlayerEntity) (Object) this, factory);
    }
}
