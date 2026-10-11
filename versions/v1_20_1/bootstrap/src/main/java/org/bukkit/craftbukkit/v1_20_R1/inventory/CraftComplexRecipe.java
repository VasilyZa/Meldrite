package org.bukkit.craftbukkit.v1_20_R1.inventory;

import dev.meldrite.v1201.compat.NativeRecipes;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftNamespacedKey;
import org.bukkit.inventory.ComplexRecipe;
import org.bukkit.inventory.ItemStack;

/** Holds the original native recipe, including custom Mod crafting and remainder logic. */
public class CraftComplexRecipe implements CraftRecipe, ComplexRecipe {
    private final net.minecraft.recipe.Recipe<?> recipe;
    public CraftComplexRecipe(net.minecraft.recipe.Recipe<?> recipe) { this.recipe = java.util.Objects.requireNonNull(recipe); }
    public CraftComplexRecipe(net.minecraft.recipe.SpecialCraftingRecipe recipe) { this((net.minecraft.recipe.Recipe<?>) recipe); }
    public net.minecraft.recipe.Recipe<?> getHandle() { return recipe; }
    @Override public NamespacedKey getKey() { return CraftNamespacedKey.fromMinecraft(recipe.getId()); }
    @Override public ItemStack getResult() { return CraftItemStack.asBukkitCopy(NativeRecipes.outputForBukkit(recipe)); }
    @Override public void addToCraftingManager() { NativeRecipes.add(NativeRecipes.server().getRecipeManager(), recipe); }
}
