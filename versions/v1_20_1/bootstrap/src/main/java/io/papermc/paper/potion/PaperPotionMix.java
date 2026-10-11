package io.papermc.paper.potion;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftRecipe;

public record PaperPotionMix(ItemStack result, Ingredient input, Ingredient ingredient) {
    public PaperPotionMix(PotionMix mix) {
        this(CraftItemStack.asNMSCopy(mix.getResult()), CraftRecipe.toIngredient(mix.getInput(), true),
                CraftRecipe.toIngredient(mix.getIngredient(), true));
    }
}
