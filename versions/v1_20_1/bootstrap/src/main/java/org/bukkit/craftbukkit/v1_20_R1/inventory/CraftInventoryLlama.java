package org.bukkit.craftbukkit.v1_20_R1.inventory;

import net.minecraft.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class CraftInventoryLlama extends CraftInventoryAbstractHorse implements org.bukkit.inventory.LlamaInventory {
    public CraftInventoryLlama(Inventory inventory) { super(inventory); }
    public CraftInventoryLlama(Inventory inventory, InventoryHolder holder) { super(inventory, holder); }
    @Override public ItemStack getDecor() { return getItem(1); }
    @Override public void setDecor(ItemStack stack) { setItem(1, stack); }
}
