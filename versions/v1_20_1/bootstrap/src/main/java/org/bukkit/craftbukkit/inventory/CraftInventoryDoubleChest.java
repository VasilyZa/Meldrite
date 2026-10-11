package org.bukkit.craftbukkit.inventory;

import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;

/** Unversioned ABI entry point for the live two-sided inventory wrapper. */
public class CraftInventoryDoubleChest extends org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventoryDoubleChest {
    public CraftInventoryDoubleChest(DoubleInventory inventory) { super(inventory); }
    public CraftInventoryDoubleChest(Inventory left, Inventory right) { super(left, right); }
}
