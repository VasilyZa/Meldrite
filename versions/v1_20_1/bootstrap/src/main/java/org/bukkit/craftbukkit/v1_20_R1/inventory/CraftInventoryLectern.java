package org.bukkit.craftbukkit.v1_20_R1.inventory;

import net.minecraft.block.entity.LecternBlockEntity;
import org.bukkit.block.Lectern;
import org.bukkit.event.inventory.InventoryType;

public final class CraftInventoryLectern extends CraftInventory {
    private final LecternBlockEntity lectern;

    public CraftInventoryLectern(LecternBlockEntity lectern, Lectern holder) {
        super(lectern.inventory, holder, InventoryType.LECTERN);
        this.lectern = lectern;
    }

    @Override public void setItem(int slot, org.bukkit.inventory.ItemStack item) {
        checkIndex(slot);
        lectern.setBook(CraftItemStack.asNMSCopy(item));
        if (lectern.getWorld() != null && lectern.getWorld().getBlockEntity(lectern.getPos()) == lectern) {
            net.minecraft.block.LecternBlock.setHasBook(null, lectern.getWorld(), lectern.getPos(), lectern.getCachedState(), lectern.hasBook());
        }
    }

    @Override public void clear() { setItem(0, null); }
}
