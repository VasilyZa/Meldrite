package org.bukkit.craftbukkit.v1_20_R1.inventory;

import dev.meldrite.v1201.compat.NativeRecipes;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.SmithingTrimRecipe;

public class CraftSmithingTrimRecipe extends SmithingTrimRecipe implements CraftRecipe {
    public CraftSmithingTrimRecipe(NamespacedKey key, RecipeChoice template, RecipeChoice base, RecipeChoice addition) { super(key, template, base, addition); }
    public CraftSmithingTrimRecipe(NamespacedKey key, RecipeChoice template, RecipeChoice base, RecipeChoice addition, boolean copyNbt) {
        super(key, template, base, addition, copyNbt);
    }
    public static CraftSmithingTrimRecipe fromBukkitRecipe(SmithingTrimRecipe recipe) {
        if (recipe instanceof CraftSmithingTrimRecipe craft) return craft;
        return (CraftSmithingTrimRecipe) NativeRecipes.copyBukkit(recipe);
    }
}
