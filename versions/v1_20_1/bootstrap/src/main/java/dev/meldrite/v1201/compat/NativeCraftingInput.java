package dev.meldrite.v1201.compat;

import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeMatcher;

public final class NativeCraftingInput extends SimpleInventory implements RecipeInputInventory {
    private final int width;
    private final int height;
    public NativeCraftingInput(int width, int height) {
        super(width * height);
        this.width = width;
        this.height = height;
    }
    @Override public int getWidth() { return width; }
    @Override public int getHeight() { return height; }
    @Override public List<ItemStack> getInputStacks() { return IntStream.range(0, size()).mapToObj(this::getStack).toList(); }
    @Override public void provideRecipeInputs(RecipeMatcher matcher) { getInputStacks().forEach(matcher::addInput); }
}
