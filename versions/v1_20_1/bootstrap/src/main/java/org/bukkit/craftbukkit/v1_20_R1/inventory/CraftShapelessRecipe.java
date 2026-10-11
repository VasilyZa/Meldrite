package org.bukkit.craftbukkit.v1_20_R1.inventory;

import dev.meldrite.v1201.compat.NativeRecipes;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapelessRecipe;

public class CraftShapelessRecipe extends ShapelessRecipe implements CraftRecipe {
    public CraftShapelessRecipe(NamespacedKey key, ItemStack result) { super(key, result); }
    public CraftShapelessRecipe(ItemStack result, net.minecraft.recipe.ShapelessRecipe recipe) {
        this(org.bukkit.craftbukkit.v1_20_R1.util.CraftNamespacedKey.fromMinecraft(recipe.getId()), result);
        NativeRecipes.fillShapeless(this, recipe);
    }
    public static CraftShapelessRecipe fromBukkitRecipe(ShapelessRecipe recipe) {
        if (recipe instanceof CraftShapelessRecipe craft) return craft;
        return (CraftShapelessRecipe) NativeRecipes.copyBukkit(recipe);
    }
}
