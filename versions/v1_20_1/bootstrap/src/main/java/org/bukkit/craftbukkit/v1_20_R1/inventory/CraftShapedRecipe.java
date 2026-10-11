package org.bukkit.craftbukkit.v1_20_R1.inventory;

import dev.meldrite.v1201.compat.NativeRecipes;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;

public class CraftShapedRecipe extends ShapedRecipe implements CraftRecipe {
    public CraftShapedRecipe(NamespacedKey key, ItemStack result) { super(key, result); }
    public CraftShapedRecipe(ItemStack result, net.minecraft.recipe.ShapedRecipe recipe) {
        this(org.bukkit.craftbukkit.v1_20_R1.util.CraftNamespacedKey.fromMinecraft(recipe.getId()), result);
        NativeRecipes.fillShaped(this, recipe);
    }
    public static CraftShapedRecipe fromBukkitRecipe(ShapedRecipe recipe) {
        if (recipe instanceof CraftShapedRecipe craft) return craft;
        return (CraftShapedRecipe) NativeRecipes.copyBukkit(recipe);
    }
}
