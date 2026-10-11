package org.bukkit.craftbukkit.v1_20_R1.inventory;

import net.minecraft.inventory.Inventory;
import org.bukkit.block.BrewingStand;
import org.bukkit.inventory.BrewerInventory;

public final class CraftInventoryBrewer extends CraftInventory implements BrewerInventory {
    private final BrewingStand holder;

    public CraftInventoryBrewer(Inventory inventory, BrewingStand holder) {
        super(inventory, holder);
        this.holder = holder;
    }

    @Override public org.bukkit.inventory.ItemStack getIngredient() { return getItem(3); }
    @Override public void setIngredient(org.bukkit.inventory.ItemStack ingredient) { setItem(3, ingredient); }
    @Override public org.bukkit.inventory.ItemStack getFuel() { return getItem(4); }
    @Override public void setFuel(org.bukkit.inventory.ItemStack fuel) { setItem(4, fuel); }
    @Override public BrewingStand getHolder() { return holder; }
}
