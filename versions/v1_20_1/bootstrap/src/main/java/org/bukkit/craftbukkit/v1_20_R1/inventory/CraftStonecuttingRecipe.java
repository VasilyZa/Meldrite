package org.bukkit.craftbukkit.v1_20_R1.inventory;

import dev.meldrite.v1201.compat.NativeRecipes;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.StonecuttingRecipe;

public class CraftStonecuttingRecipe extends StonecuttingRecipe implements CraftRecipe {
    public CraftStonecuttingRecipe(NamespacedKey key, ItemStack result, RecipeChoice source) { super(key, result, source); }
    public static CraftStonecuttingRecipe fromBukkitRecipe(StonecuttingRecipe recipe) {
        if (recipe instanceof CraftStonecuttingRecipe craft) return craft;
        return (CraftStonecuttingRecipe) NativeRecipes.copyBukkit(recipe);
    }
}
