package dev.meldrite.v1201.hook;

import java.util.List;
import net.minecraft.recipe.BrewingRecipeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BrewingRecipeRegistry.class)
public interface BrewingRecipeListsAccessMixin {
    @Accessor("POTION_RECIPES") static List<?> meldrite$potionRecipes() { throw new AssertionError("Mixin not applied"); }
    @Accessor("ITEM_RECIPES") static List<?> meldrite$itemRecipes() { throw new AssertionError("Mixin not applied"); }
    @Accessor("POTION_TYPES") static List<?> meldrite$potionTypes() { throw new AssertionError("Mixin not applied"); }
}
