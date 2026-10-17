package dev.meldrite.v1201.hook;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Applies CraftBukkit's exact item/NBT choice flag to native and Fabric recipe matching. */
@Mixin(Ingredient.class)
public abstract class IngredientExactMatchMixin {
    @Shadow public abstract boolean isEmpty();
    @Shadow public abstract ItemStack[] getMatchingStacks();

    public boolean exact;

    // Fabric's shapeless matcher must use the ingredient predicate instead of
    // reducing an exact choice to item IDs. Custom ingredient subclasses retain
    // their own requiresTesting implementation.
    public boolean requiresTesting() {
        return exact;
    }

    @Inject(method = "test(Lnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void meldrite$exactChoice(ItemStack stack, CallbackInfoReturnable<Boolean> callback) {
        if (!exact) return;
        if (stack == null) {
            callback.setReturnValue(false);
            return;
        }
        if (isEmpty()) return; // Preserve the native empty-ingredient contract.
        for (ItemStack choice : getMatchingStacks()) {
            if (ItemStack.canCombine(choice, stack)) {
                callback.setReturnValue(true);
                return;
            }
        }
        callback.setReturnValue(false);
    }
}
