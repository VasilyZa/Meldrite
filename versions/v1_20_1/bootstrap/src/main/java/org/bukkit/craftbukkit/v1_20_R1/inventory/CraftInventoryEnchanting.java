package org.bukkit.craftbukkit.v1_20_R1.inventory;

import org.bukkit.inventory.ItemStack;

public class CraftInventoryEnchanting extends CraftInventory implements org.bukkit.inventory.EnchantingInventory {
    public CraftInventoryEnchanting(net.minecraft.inventory.Inventory inventory) { super(inventory, null, org.bukkit.event.inventory.InventoryType.ENCHANTING); }
    @Override public ItemStack getItem() { return getItem(0); }
    @Override public void setItem(ItemStack item) { setItem(0, item); }
    @Override public ItemStack getSecondary() { return getItem(1); }
    @Override public void setSecondary(ItemStack item) { setItem(1, item); }
}
