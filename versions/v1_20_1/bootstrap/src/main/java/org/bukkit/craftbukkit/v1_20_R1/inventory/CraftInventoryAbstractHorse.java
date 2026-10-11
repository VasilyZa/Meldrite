package org.bukkit.craftbukkit.v1_20_R1.inventory;

import net.minecraft.inventory.Inventory;
import org.bukkit.inventory.AbstractHorseInventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class CraftInventoryAbstractHorse extends CraftInventory implements AbstractHorseInventory {
    public CraftInventoryAbstractHorse(Inventory inventory) { super(inventory); }
    public CraftInventoryAbstractHorse(Inventory inventory, InventoryHolder holder) { super(inventory, holder); }
    @Override public ItemStack getSaddle() { return getItem(0); }
    @Override public void setSaddle(ItemStack stack) { setItem(0, stack); }
    @Override public org.bukkit.Location getLocation() { return getHolder() instanceof org.bukkit.entity.AbstractHorse horse ? horse.getLocation() : super.getLocation(); }
}
