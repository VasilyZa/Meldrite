package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeRecipes;
import net.minecraft.recipe.Recipe;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Recipe.class)
public interface RecipeBukkitMixin {
    default org.bukkit.inventory.Recipe toBukkitRecipe() {
        return NativeRecipes.fromMinecraft((Recipe<?>) this);
    }
}
