package org.bukkit.craftbukkit.v1_20_R1.inventory;

import net.minecraft.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class CraftInventoryHorse extends CraftInventoryAbstractHorse implements org.bukkit.inventory.HorseInventory {
    public CraftInventoryHorse(Inventory inventory) { super(inventory); }
    public CraftInventoryHorse(Inventory inventory, InventoryHolder holder) { super(inventory, holder); }
    @Override public ItemStack getArmor() { return getItem(1); }
    @Override public void setArmor(ItemStack stack) { setItem(1, stack); }
}
