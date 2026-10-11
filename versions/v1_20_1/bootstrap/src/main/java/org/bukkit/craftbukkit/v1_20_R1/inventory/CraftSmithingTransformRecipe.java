package org.bukkit.craftbukkit.v1_20_R1.inventory;

import dev.meldrite.v1201.compat.NativeRecipes;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.SmithingTransformRecipe;

public class CraftSmithingTransformRecipe extends SmithingTransformRecipe implements CraftRecipe {
    public CraftSmithingTransformRecipe(NamespacedKey key, ItemStack result, RecipeChoice template, RecipeChoice base, RecipeChoice addition) { super(key, result, template, base, addition); }
    public CraftSmithingTransformRecipe(NamespacedKey key, ItemStack result, RecipeChoice template, RecipeChoice base, RecipeChoice addition, boolean copyNbt) {
        super(key, result, template, base, addition, copyNbt);
    }
    public static CraftSmithingTransformRecipe fromBukkitRecipe(SmithingTransformRecipe recipe) {
        if (recipe instanceof CraftSmithingTransformRecipe craft) return craft;
        return (CraftSmithingTransformRecipe) NativeRecipes.copyBukkit(recipe);
    }
}
