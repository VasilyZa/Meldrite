package dev.meldrite.v1201.hook;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.recipe.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** Stores the current recipe on the crafting matrix that produced it. */
@Mixin(CraftingInventory.class)
public abstract class CraftingInventoryRecipeMixin {
    @Unique private Recipe<?> meldrite$currentRecipe;

    public Recipe<?> getCurrentRecipe() {
        return meldrite$currentRecipe;
    }

    public void setCurrentRecipe(Recipe<?> recipe) {
        meldrite$currentRecipe = recipe;
    }
}
