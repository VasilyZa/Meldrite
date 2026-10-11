package dev.meldrite.v1201.compat;

import net.minecraft.recipe.Ingredient;
public interface SmithingRecipeAccess {
    Ingredient meldrite$getTemplate();
    Ingredient meldrite$getBase();
    Ingredient meldrite$getAddition();
}
