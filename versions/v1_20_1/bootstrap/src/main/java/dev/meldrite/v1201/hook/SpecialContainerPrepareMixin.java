package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.InventoryEventBridge;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.CartographyTableScreenHandler;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.screen.LoomScreenHandler;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({CartographyTableScreenHandler.class, GrindstoneScreenHandler.class, LoomScreenHandler.class})
public abstract class SpecialContainerPrepareMixin {
    @Inject(method = "onContentChanged", at = @At("RETURN"))
    private void meldrite$prepare(Inventory inventory, CallbackInfo ci) {
        var handler = (ScreenHandler) (Object) this;
        InventoryEventBridge.prepareResult(handler, handler instanceof LoomScreenHandler ? 3 : 2);
    }
}
