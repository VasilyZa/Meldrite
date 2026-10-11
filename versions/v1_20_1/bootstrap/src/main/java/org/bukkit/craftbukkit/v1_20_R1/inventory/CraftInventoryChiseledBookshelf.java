package org.bukkit.craftbukkit.v1_20_R1.inventory;

import net.minecraft.block.entity.ChiseledBookshelfBlockEntity;
import org.bukkit.block.ChiseledBookshelf;

public class CraftInventoryChiseledBookshelf extends CraftInventory implements org.bukkit.inventory.ChiseledBookshelfInventory {
    public CraftInventoryChiseledBookshelf(ChiseledBookshelfBlockEntity tile, ChiseledBookshelf holder) { super(tile, holder, org.bukkit.event.inventory.InventoryType.CHISELED_BOOKSHELF); }
    @Override public ChiseledBookshelf getHolder() { return (ChiseledBookshelf) super.getHolder(); }

    @Override public void setItem(int slot, org.bukkit.inventory.ItemStack item) {
        if (slot < 0 || slot >= getSize()) throw new IndexOutOfBoundsException("slot: " + slot);
        var tile = (ChiseledBookshelfBlockEntity) inventory;
        var replacement = CraftItemStack.asNMSCopy(item);
        if (tile.getWorld() != null) {
            if (replacement.isEmpty()) { inventory.removeStack(slot); inventory.markDirty(); }
            else super.setItem(slot, item);
            return;
        }
        if (!replacement.isEmpty() && !replacement.isIn(net.minecraft.registry.tag.ItemTags.BOOKSHELF_BOOKS)) return;
        var nbt = tile.createNbt();
        var contents = net.minecraft.util.collection.DefaultedList.ofSize(getSize(), net.minecraft.item.ItemStack.EMPTY);
        net.minecraft.inventory.Inventories.readNbt(nbt, contents);
        contents.set(slot, replacement);
        net.minecraft.inventory.Inventories.writeNbt(nbt, contents);
        nbt.putInt("last_interacted_slot", slot);
        tile.readNbt(nbt);
    }
}
