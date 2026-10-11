package dev.meldrite.v1201.hook;

import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.SmithingTrimRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SmithingTrimRecipe.class)
public interface SmithingTrimRecipeAccess extends dev.meldrite.v1201.compat.SmithingRecipeAccess {
    @Override @Accessor("template") Ingredient meldrite$getTemplate();
    @Override @Accessor("base") Ingredient meldrite$getBase();
    @Override @Accessor("addition") Ingredient meldrite$getAddition();
}
