package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeBrewing;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.screen.BrewingStandScreenHandler$PotionSlot")
public abstract class BrewingPotionSlotMixin {
    @Inject(method = "matches", at = @At("HEAD"), cancellable = true)
    private static void meldrite$input(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (NativeBrewing.input(stack)) cir.setReturnValue(true);
    }
}
