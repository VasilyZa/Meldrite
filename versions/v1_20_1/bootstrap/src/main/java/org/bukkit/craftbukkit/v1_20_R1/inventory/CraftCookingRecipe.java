package org.bukkit.craftbukkit.v1_20_R1.inventory;

import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftNamespacedKey;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.RecipeChoice;

public final class CraftCookingRecipe {
    private CraftCookingRecipe() { }

    public static CookingRecipe<?> fromMinecraft(AbstractCookingRecipe nativeRecipe, DynamicRegistryManager registries) {
        var key = CraftNamespacedKey.fromMinecraft(nativeRecipe.getId());
        var result = CraftItemStack.asBukkitCopy(nativeRecipe.getOutput(registries));
        RecipeChoice choice = CraftRecipe.toBukkit(nativeRecipe.getIngredients().get(0));
        CookingRecipe<?> recipe;
        if (nativeRecipe.getType() == RecipeType.SMELTING) {
            recipe = new CraftFurnaceRecipe(key, result, choice, nativeRecipe.getExperience(), nativeRecipe.getCookTime());
        } else if (nativeRecipe.getType() == RecipeType.BLASTING) {
            recipe = new CraftBlastingRecipe(key, result, choice, nativeRecipe.getExperience(), nativeRecipe.getCookTime());
        } else if (nativeRecipe.getType() == RecipeType.SMOKING) {
            recipe = new CraftSmokingRecipe(key, result, choice, nativeRecipe.getExperience(), nativeRecipe.getCookTime());
        } else if (nativeRecipe.getType() == RecipeType.CAMPFIRE_COOKING) {
            recipe = new CraftCampfireRecipe(key, result, choice, nativeRecipe.getExperience(), nativeRecipe.getCookTime());
        } else {
            throw new UnsupportedOperationException("Unmapped native cooking recipe type: " + nativeRecipe.getType());
        }
        recipe.setGroup(nativeRecipe.getGroup());
        recipe.setCategory(org.bukkit.inventory.recipe.CookingBookCategory.valueOf(nativeRecipe.getCategory().name()));
        return recipe;
    }

}
