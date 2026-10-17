package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeRecipeManagerAccess;
import dev.meldrite.v1201.compat.NativeInventoryRecipeAccess;
import dev.meldrite.v1201.compat.NativeRecipes;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.Optional;
import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

    @Inject(method = "getFirstMatch(Lnet/minecraft/recipe/RecipeType;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/world/World;)Ljava/util/Optional;",
            at = @At("RETURN"), require = 1)
    private <C extends Inventory, T extends Recipe<C>> void meldrite$setCurrentRecipe(
            RecipeType<T> type, C inventory, World world, CallbackInfoReturnable<Optional<T>> callback) {
        ((NativeInventoryRecipeAccess) inventory).meldrite$setCurrentRecipe(callback.getReturnValue().orElse(null));
    }

    @Inject(method = "getFirstMatch(Lnet/minecraft/recipe/RecipeType;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/world/World;Lnet/minecraft/util/Identifier;)Ljava/util/Optional;",
            at = @At("RETURN"), require = 1)
    private <C extends Inventory, T extends Recipe<C>> void meldrite$setCurrentRecipeForCachedId(
            RecipeType<T> type, C inventory, World world, Identifier cachedId,
            CallbackInfoReturnable<Optional<Pair<Identifier, T>>> callback) {
        Recipe<?> recipe = callback.getReturnValue().map(Pair::getSecond).orElse(null);
        ((NativeInventoryRecipeAccess) inventory).meldrite$setCurrentRecipe(recipe);
    }
}
