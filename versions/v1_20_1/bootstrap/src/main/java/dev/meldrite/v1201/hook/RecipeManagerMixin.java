package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeRecipeManagerAccess;
import dev.meldrite.v1201.compat.NativeRecipes;
import java.util.Map;
import java.util.Optional;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin implements NativeRecipeManagerAccess {
    @Shadow private Map<RecipeType<?>, Map<Identifier, Recipe<?>>> recipes;
    @Shadow private Map<Identifier, Recipe<?>> recipesById;
    @Override public Map<RecipeType<?>, Map<Identifier, Recipe<?>>> meldrite$getRecipes() { return recipes; }
    @Override public void meldrite$setRecipes(Map<RecipeType<?>, Map<Identifier, Recipe<?>>> value) { recipes = value; }
    @Override public Map<Identifier, Recipe<?>> meldrite$getRecipesById() { return recipesById; }
    @Override public void meldrite$setRecipesById(Map<Identifier, Recipe<?>> value) { recipesById = value; }
    public void addRecipe(Recipe<?> recipe) { NativeRecipes.add((RecipeManager) (Object) this, recipe); }
    public boolean removeRecipe(Identifier id) { return NativeRecipes.remove((RecipeManager) (Object) this, id); }
    public void clearRecipes() { NativeRecipes.clear((RecipeManager) (Object) this); }
    public Optional<Recipe<?>> byKey(Identifier id) { return Optional.ofNullable(recipesById.get(id)); }
}
