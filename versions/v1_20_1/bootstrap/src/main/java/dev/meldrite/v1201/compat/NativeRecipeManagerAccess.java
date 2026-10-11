package dev.meldrite.v1201.compat;

import java.util.Map;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;

public interface NativeRecipeManagerAccess {
    Map<RecipeType<?>, Map<Identifier, Recipe<?>>> meldrite$getRecipes();
    void meldrite$setRecipes(Map<RecipeType<?>, Map<Identifier, Recipe<?>>> recipes);
    Map<Identifier, Recipe<?>> meldrite$getRecipesById();
    void meldrite$setRecipesById(Map<Identifier, Recipe<?>> recipes);
}
