package org.bukkit.craftbukkit.v1_20_R1.inventory;

import dev.meldrite.v1201.compat.NativeRecipes;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.CampfireRecipe;

public class CraftCampfireRecipe extends CampfireRecipe implements CraftRecipe {
    public CraftCampfireRecipe(NamespacedKey key, ItemStack result, RecipeChoice source, float experience, int cookingTime) { super(key, result, source, experience, cookingTime); }
    public static CraftCampfireRecipe fromBukkitRecipe(CampfireRecipe recipe) {
        if (recipe instanceof CraftCampfireRecipe craft) return craft;
        return (CraftCampfireRecipe) NativeRecipes.copyBukkit(recipe);
    }
}
