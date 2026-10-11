package org.bukkit.craftbukkit.v1_20_R1.inventory;

import net.minecraft.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class CraftSaddledInventory extends CraftInventoryAbstractHorse implements org.bukkit.inventory.SaddledHorseInventory {
    public CraftSaddledInventory(Inventory inventory) { super(inventory); }
    public CraftSaddledInventory(Inventory inventory, InventoryHolder holder) { super(inventory, holder); }
}
