package org.bukkit.craftbukkit.v1_20_R1.inventory;

public class CraftInventoryGrindstone extends CraftResultInventory implements org.bukkit.inventory.GrindstoneInventory {
    public CraftInventoryGrindstone(net.minecraft.inventory.Inventory inventory, net.minecraft.inventory.Inventory result) {
        super(inventory, result, org.bukkit.event.inventory.InventoryType.GRINDSTONE);
    }
}
