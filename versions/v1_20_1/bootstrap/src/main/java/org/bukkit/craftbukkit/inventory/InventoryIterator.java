package org.bukkit.craftbukkit.inventory;

import java.util.ListIterator;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/** Live list iterator over an Inventory's slots. */
public class InventoryIterator implements ListIterator<ItemStack> {
    private final Inventory inventory;
    private int nextIndex;
    private Boolean forward;

    InventoryIterator(Inventory inventory) { this(inventory, 0); }
    InventoryIterator(Inventory inventory, int index) { this.inventory = inventory; this.nextIndex = index; }
    @Override public boolean hasNext() { return nextIndex < inventory.getSize(); }
    @Override public ItemStack next() { forward = true; return inventory.getItem(nextIndex++); }
    @Override public int nextIndex() { return nextIndex; }
    @Override public boolean hasPrevious() { return nextIndex > 0; }
    @Override public ItemStack previous() { forward = false; return inventory.getItem(--nextIndex); }
    @Override public int previousIndex() { return nextIndex - 1; }
    @Override public void set(ItemStack item) { if (forward == null) throw new IllegalStateException("No current item"); inventory.setItem(forward ? nextIndex - 1 : nextIndex, item); }
    @Override public void add(ItemStack item) { throw new UnsupportedOperationException("An inventory has a fixed size"); }
    @Override public void remove() { throw new UnsupportedOperationException("An inventory has a fixed size"); }
}
