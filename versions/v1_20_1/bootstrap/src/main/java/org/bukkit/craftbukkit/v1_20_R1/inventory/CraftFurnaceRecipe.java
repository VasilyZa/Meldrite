package org.bukkit.craftbukkit.v1_20_R1.inventory;

import dev.meldrite.v1201.compat.NativeRecipes;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.FurnaceRecipe;

public class CraftFurnaceRecipe extends FurnaceRecipe implements CraftRecipe {
    public CraftFurnaceRecipe(NamespacedKey key, ItemStack result, RecipeChoice source, float experience, int cookingTime) { super(key, result, source, experience, cookingTime); }
    public static CraftFurnaceRecipe fromBukkitRecipe(FurnaceRecipe recipe) {
        if (recipe instanceof CraftFurnaceRecipe craft) return craft;
        return (CraftFurnaceRecipe) NativeRecipes.copyBukkit(recipe);
    }
}
