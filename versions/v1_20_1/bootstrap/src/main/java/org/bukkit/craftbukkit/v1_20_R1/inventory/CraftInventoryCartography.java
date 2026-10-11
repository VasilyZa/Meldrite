package org.bukkit.craftbukkit.v1_20_R1.inventory;

public class CraftInventoryCartography extends CraftResultInventory implements org.bukkit.inventory.CartographyInventory {
    public CraftInventoryCartography(net.minecraft.inventory.Inventory inventory, net.minecraft.inventory.Inventory result) {
        super(inventory, result, org.bukkit.event.inventory.InventoryType.CARTOGRAPHY);
    }
}
