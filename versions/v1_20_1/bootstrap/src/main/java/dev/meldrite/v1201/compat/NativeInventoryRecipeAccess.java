package dev.meldrite.v1201.compat;

import net.minecraft.recipe.Recipe;

/** Native access to Paper's current-recipe state on inventories. */
public interface NativeInventoryRecipeAccess {
    Recipe<?> meldrite$getCurrentRecipe();

    void meldrite$setCurrentRecipe(Recipe<?> recipe);
}
