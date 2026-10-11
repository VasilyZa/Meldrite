package org.bukkit.craftbukkit.v1_20_R1.inventory;

import dev.meldrite.v1201.compat.NativeRecipes;
import java.util.Objects;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

public class CraftInventoryCrafting extends CraftInventory implements CraftingInventory {
    private final Inventory resultInventory;
    public CraftInventoryCrafting(Inventory matrix, Inventory result) { this(matrix, result, null); }
    public CraftInventoryCrafting(Inventory matrix, Inventory result, InventoryHolder holder) {
        this(matrix, result, holder, matrix.size() == 4 ? InventoryType.CRAFTING.getDefaultTitle() : InventoryType.WORKBENCH.getDefaultTitle());
    }
    public CraftInventoryCrafting(Inventory matrix, Inventory result, InventoryHolder holder, String title) {
        super(matrix, holder, matrix.size() == 4 ? InventoryType.CRAFTING : InventoryType.WORKBENCH, title);
        resultInventory = Objects.requireNonNull(result, "result");
    }
    public Inventory getMatrixInventory() { return inventory; }
    public Inventory getResultInventory() { return resultInventory; }
    @Override public int getSize() { return inventory.size() + resultInventory.size(); }
    @Override public CraftItemStack getItem(int index) {
        if (index < 0 || index >= getSize()) throw new IndexOutOfBoundsException(index);
        return (CraftItemStack) craftStack(index < resultInventory.size() ? resultInventory.getStack(index) : inventory.getStack(index - resultInventory.size()));
    }
    @Override public void setItem(int index, ItemStack stack) {
        if (index < 0 || index >= getSize()) throw new IndexOutOfBoundsException(index);
        if (index < resultInventory.size()) resultInventory.setStack(index, nativeStack(stack));
        else inventory.setStack(index - resultInventory.size(), nativeStack(stack));
    }
    @Override public ItemStack[] getContents() {
        var contents = new ItemStack[getSize()];
        for (int i = 0; i < contents.length; i++) contents[i] = getItem(i);
        return contents;
    }
    @Override public ItemStack[] getMatrix() { return mirrorContents(inventory.size()); }
    @Override public void setMatrix(ItemStack[] matrix) {
        Objects.requireNonNull(matrix, "matrix");
        if (matrix.length > inventory.size()) throw new IllegalArgumentException("Invalid crafting matrix size");
        for (int i = 0; i < inventory.size(); i++) inventory.setStack(i, nativeStack(i < matrix.length ? matrix[i] : null));
    }
    public void setContents(ItemStack result, ItemStack[] matrix) { setResult(result); setMatrix(matrix); }
    @Override public ItemStack getResult() { return craftStack(resultInventory.getStack(0)); }
    @Override public void setResult(ItemStack result) { resultInventory.setStack(0, nativeStack(result)); }
    @Override public void clear() { inventory.clear(); resultInventory.clear(); }
    @Override public Recipe getRecipe() {
        var recipe = resultInventory instanceof CraftingResultInventory result ? result.getLastRecipe() : null;
        return recipe == null ? null : NativeRecipes.fromMinecraft(recipe);
    }
}
