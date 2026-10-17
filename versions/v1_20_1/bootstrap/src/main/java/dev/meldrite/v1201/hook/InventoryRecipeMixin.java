package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeInventoryRecipeAccess;
import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.Recipe;
import org.spongepowered.asm.mixin.Mixin;

/** Paper's explicit no-recipe default contract for inventories without recipe state. */
@Mixin(Inventory.class)
public interface InventoryRecipeMixin extends NativeInventoryRecipeAccess {
    default Recipe<?> getCurrentRecipe() {
        return null;
    }

    default void setCurrentRecipe(Recipe<?> recipe) {
        // Generic inventories do not retain recipe state.
    }

    @Override
    default Recipe<?> meldrite$getCurrentRecipe() {
        return getCurrentRecipe();
    }

    @Override
    default void meldrite$setCurrentRecipe(Recipe<?> recipe) {
        setCurrentRecipe(recipe);
    }
}
