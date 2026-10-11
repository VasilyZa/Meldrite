package org.bukkit.craftbukkit.v1_20_R1.inventory;

import dev.meldrite.v1201.compat.NativeRecipes;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.BlastingRecipe;

public class CraftBlastingRecipe extends BlastingRecipe implements CraftRecipe {
    public CraftBlastingRecipe(NamespacedKey key, ItemStack result, RecipeChoice source, float experience, int cookingTime) { super(key, result, source, experience, cookingTime); }
    public static CraftBlastingRecipe fromBukkitRecipe(BlastingRecipe recipe) {
        if (recipe instanceof CraftBlastingRecipe craft) return craft;
        return (CraftBlastingRecipe) NativeRecipes.copyBukkit(recipe);
    }
}
