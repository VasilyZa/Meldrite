package org.bukkit.craftbukkit.v1_20_R1.inventory;

import dev.meldrite.v1201.compat.NativeRecipes;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.SmokingRecipe;

public class CraftSmokingRecipe extends SmokingRecipe implements CraftRecipe {
    public CraftSmokingRecipe(NamespacedKey key, ItemStack result, RecipeChoice source, float experience, int cookingTime) { super(key, result, source, experience, cookingTime); }
    public static CraftSmokingRecipe fromBukkitRecipe(SmokingRecipe recipe) {
        if (recipe instanceof CraftSmokingRecipe craft) return craft;
        return (CraftSmokingRecipe) NativeRecipes.copyBukkit(recipe);
    }
}
