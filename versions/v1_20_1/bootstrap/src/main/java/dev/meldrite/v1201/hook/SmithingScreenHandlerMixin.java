package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.InventoryEventBridge;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.screen.SmithingScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SmithingScreenHandler.class)
public abstract class SmithingScreenHandlerMixin {
    @Shadow private SmithingRecipe currentRecipe;
    @Inject(method = "updateResult", at = @At("HEAD"))
    private void meldrite$clearRecipe(CallbackInfo ci) {
        currentRecipe = null;
        var handler = (SmithingScreenHandler) (Object) this;
        ((CraftingResultInventory) handler.getSlot(handler.getResultSlotIndex()).inventory).setLastRecipe(null);
    }
    @Inject(method = "updateResult", at = @At("RETURN"))
    private void meldrite$prepare(CallbackInfo ci) {
        var handler = (SmithingScreenHandler) (Object) this;
        InventoryEventBridge.prepareResult(((ForgingScreenHandlerPaperAccessMixin) this).meldrite$getPlayer(), handler, handler.getResultSlotIndex());
    }
}
