package org.bukkit.craftbukkit.v1_20_R1.inventory;

import dev.meldrite.v1201.compat.NativeRecipes;
import java.util.ArrayList;
import java.util.Iterator;
import net.minecraft.recipe.RecipeManager;
import org.bukkit.inventory.Recipe;

public class RecipeIterator implements Iterator<Recipe> {
    private final RecipeManager manager;
    private final Iterator<net.minecraft.recipe.Recipe<?>> recipes;
    private net.minecraft.recipe.Recipe<?> current;
    public RecipeIterator() { this(NativeRecipes.server().getRecipeManager()); }
    public RecipeIterator(RecipeManager manager) {
        this.manager = manager;
        this.recipes = new ArrayList<>(manager.values()).iterator();
    }
    @Override public boolean hasNext() { return recipes.hasNext(); }
    @Override public Recipe next() { current = recipes.next(); return NativeRecipes.fromMinecraft(current); }
    @Override public void remove() {
        if (current == null) throw new IllegalStateException("next() must precede remove()");
        NativeRecipes.remove(manager, current.getId());
        current = null;
    }
}
