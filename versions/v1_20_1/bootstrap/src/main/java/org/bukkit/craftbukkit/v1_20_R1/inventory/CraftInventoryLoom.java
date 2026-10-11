package org.bukkit.craftbukkit.v1_20_R1.inventory;

public class CraftInventoryLoom extends CraftResultInventory implements org.bukkit.inventory.LoomInventory {
    public CraftInventoryLoom(net.minecraft.inventory.Inventory inventory, net.minecraft.inventory.Inventory result) {
        super(inventory, result, org.bukkit.event.inventory.InventoryType.LOOM);
    }
}
