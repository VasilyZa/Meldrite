package org.bukkit.craftbukkit.v1_20_R1.inventory;

import org.bukkit.block.BlockState;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/** Shared holder used when a block state exposes a live inventory. */
public class CraftBlockInventoryHolder implements InventoryHolder {
    private final BlockState state;
    private final Inventory inventory;
    public CraftBlockInventoryHolder(BlockState state, Inventory inventory) {
        this.state = java.util.Objects.requireNonNull(state, "state");
        this.inventory = java.util.Objects.requireNonNull(inventory, "inventory");
    }
    public BlockState getBlockState() { return state; }
    @Override public Inventory getInventory() { return inventory; }
}
