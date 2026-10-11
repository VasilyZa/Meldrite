package org.bukkit.craftbukkit.v1_20_R1.inventory;

public class CraftInventoryStonecutter extends CraftResultInventory implements org.bukkit.inventory.StonecutterInventory {
    public CraftInventoryStonecutter(net.minecraft.inventory.Inventory inventory, net.minecraft.inventory.Inventory result) {
        super(inventory, result, org.bukkit.event.inventory.InventoryType.STONECUTTER);
    }
}
