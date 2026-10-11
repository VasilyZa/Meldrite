package org.bukkit.craftbukkit.v1_20_R1.inventory;

import dev.meldrite.v1201.compat.NativeRecipes;
import java.util.Arrays;
import net.fabricmc.fabric.api.recipe.v1.ingredient.DefaultCustomIngredients;
import net.minecraft.recipe.Ingredient;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;

public interface CraftRecipe extends Recipe {
    default void addToCraftingManager() {
        NativeRecipes.add(NativeRecipes.server().getRecipeManager(), NativeRecipes.toMinecraft(this));
    }

    default Ingredient toNMS(RecipeChoice choice, boolean requireNotEmpty) {
        return toIngredient(choice, requireNotEmpty);
    }

    static Ingredient toIngredient(RecipeChoice choice, boolean requireNotEmpty) {
        Ingredient ingredient;
        if (choice == null) ingredient = Ingredient.EMPTY;
        else if (choice instanceof NativeChoice nativeChoice) ingredient = nativeChoice.ingredient;
        else if (choice instanceof NativeExactChoice nativeChoice) ingredient = nativeChoice.ingredient;
        else if (choice instanceof RecipeChoice.MaterialChoice materials) {
            ingredient = Ingredient.ofStacks(materials.getChoices().stream()
                    .map(material -> CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(material))));
        } else if (choice instanceof RecipeChoice.ExactChoice exact) {
            ingredient = DefaultCustomIngredients.any(exact.getChoices().stream()
                    .map(stack -> DefaultCustomIngredients.nbt(CraftItemStack.asNMSCopy(stack), true))
                    .toArray(Ingredient[]::new));
        } else throw new IllegalArgumentException("Unknown recipe choice: " + choice.getClass().getName());
        if (requireNotEmpty && Arrays.stream(ingredient.getMatchingStacks()).noneMatch(stack -> !stack.isEmpty())) {
            throw new IllegalArgumentException("Recipe requires at least one non-air choice");
        }
        return ingredient;
    }

    static RecipeChoice toBukkit(Ingredient ingredient) {
        if (ingredient.getMatchingStacks().length == 0) return null;
        return ingredient.requiresTesting() ? new NativeExactChoice(ingredient) : new NativeChoice(ingredient);
    }

    static net.minecraft.recipe.book.CraftingRecipeCategory getCategory(org.bukkit.inventory.recipe.CraftingBookCategory category) {
        return net.minecraft.recipe.book.CraftingRecipeCategory.valueOf(category.name());
    }
    static org.bukkit.inventory.recipe.CraftingBookCategory getCategory(net.minecraft.recipe.book.CraftingRecipeCategory category) {
        return org.bukkit.inventory.recipe.CraftingBookCategory.valueOf(category.name());
    }
    static net.minecraft.recipe.book.CookingRecipeCategory getCategory(org.bukkit.inventory.recipe.CookingBookCategory category) {
        return net.minecraft.recipe.book.CookingRecipeCategory.valueOf(category.name());
    }
    static org.bukkit.inventory.recipe.CookingBookCategory getCategory(net.minecraft.recipe.book.CookingRecipeCategory category) {
        return org.bukkit.inventory.recipe.CookingBookCategory.valueOf(category.name());
    }

    /** Retains tag and Mod ingredient predicates when a native recipe is inspected and registered again. */
    final class NativeChoice extends RecipeChoice.MaterialChoice {
        private final Ingredient ingredient;
        private NativeChoice(Ingredient ingredient) {
            super(Arrays.stream(ingredient.getMatchingStacks())
                    .map(stack -> CraftItemStack.asCraftMirror(stack).getType()).distinct().toList());
            this.ingredient = ingredient;
        }
        @Override public boolean test(org.bukkit.inventory.ItemStack item) {
            return ingredient.test(CraftItemStack.asNMSCopy(item));
        }
        @Override public NativeChoice clone() { return new NativeChoice(ingredient); }
    }

    final class NativeExactChoice extends RecipeChoice.ExactChoice {
        private final Ingredient ingredient;
        private NativeExactChoice(Ingredient ingredient) {
            super(Arrays.stream(ingredient.getMatchingStacks()).map(CraftItemStack::asBukkitCopy).toList());
            this.ingredient = ingredient;
        }
        @Override public boolean test(org.bukkit.inventory.ItemStack item) { return ingredient.test(CraftItemStack.asNMSCopy(item)); }
        @Override public NativeExactChoice clone() { return new NativeExactChoice(ingredient); }
    }
}
