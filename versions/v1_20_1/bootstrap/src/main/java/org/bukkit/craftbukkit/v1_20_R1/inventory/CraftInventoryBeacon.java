package org.bukkit.craftbukkit.v1_20_R1.inventory;

import org.bukkit.inventory.ItemStack;

public class CraftInventoryBeacon extends CraftInventory implements org.bukkit.inventory.BeaconInventory {
    public CraftInventoryBeacon(net.minecraft.inventory.Inventory inventory) { super(inventory, null, org.bukkit.event.inventory.InventoryType.BEACON); }
    @Override public ItemStack getItem() { return getItem(0); }
    @Override public void setItem(ItemStack item) { setItem(0, item); }
}
