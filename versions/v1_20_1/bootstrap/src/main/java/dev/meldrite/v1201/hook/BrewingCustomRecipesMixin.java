package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeBrewing;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.BrewingRecipeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BrewingRecipeRegistry.class)
public abstract class BrewingCustomRecipesMixin {
    @Inject(method = "isValidIngredient", at = @At("HEAD"), cancellable = true)
    private static void meldrite$ingredient(ItemStack stack, CallbackInfoReturnable<Boolean> cir) { if (NativeBrewing.ingredient(stack)) cir.setReturnValue(true); }
    @Inject(method = "hasRecipe", at = @At("HEAD"), cancellable = true)
    private static void meldrite$matches(ItemStack input, ItemStack ingredient, CallbackInfoReturnable<Boolean> cir) { if (NativeBrewing.matches(input, ingredient)) cir.setReturnValue(true); }
    @Inject(method = "craft", at = @At("HEAD"), cancellable = true)
    private static void meldrite$craft(ItemStack ingredient, ItemStack input, CallbackInfoReturnable<ItemStack> cir) {
        var result = NativeBrewing.craft(input, ingredient); if (result != null) cir.setReturnValue(result);
    }
}
