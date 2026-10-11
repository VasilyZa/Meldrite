package org.bukkit.craftbukkit.v1_20_R1.inventory;

import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

public class CraftInventorySmithing extends CraftResultInventory implements org.bukkit.inventory.SmithingInventory {
    private final Location location;
    public CraftInventorySmithing(Location location, Inventory inventory, CraftingResultInventory result) {
        super(inventory, result, org.bukkit.event.inventory.InventoryType.SMITHING);
        this.location = location;
    }
    @Override public Location getLocation() { return location == null ? null : location.clone(); }
    @Override public CraftingResultInventory getResultInventory() { return (CraftingResultInventory) super.getResultInventory(); }
    @Override public ItemStack getResult() { return craftStack(getResultInventory().getStack(0)); }
    @Override public void setResult(ItemStack item) { getResultInventory().setStack(0, nativeStack(item)); }
    @Override public Recipe getRecipe() {
        var recipe = getResultInventory().getLastRecipe();
        return recipe == null ? null : dev.meldrite.v1201.compat.NativeRecipes.fromMinecraft(recipe);
    }
}
