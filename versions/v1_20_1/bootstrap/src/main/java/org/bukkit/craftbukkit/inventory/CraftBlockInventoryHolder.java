package org.bukkit.craftbukkit.inventory;

import org.bukkit.block.BlockState;
import org.bukkit.inventory.Inventory;

/** Unversioned ABI entry point for block inventory holders. */
public final class CraftBlockInventoryHolder extends org.bukkit.craftbukkit.v1_20_R1.inventory.CraftBlockInventoryHolder {
    public CraftBlockInventoryHolder(BlockState state, Inventory inventory) { super(state, inventory); }
}
