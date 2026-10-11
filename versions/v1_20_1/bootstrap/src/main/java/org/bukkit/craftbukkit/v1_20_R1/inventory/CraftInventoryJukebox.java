package org.bukkit.craftbukkit.v1_20_R1.inventory;

import net.minecraft.block.entity.JukeboxBlockEntity;
import org.bukkit.block.Jukebox;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.JukeboxInventory;

public final class CraftInventoryJukebox extends CraftInventory implements JukeboxInventory {
    private final Jukebox holder;

    public CraftInventoryJukebox(JukeboxBlockEntity inventory, Jukebox holder) {
        super(inventory, holder, InventoryType.JUKEBOX);
        this.holder = holder;
    }

    @Override public void setRecord(ItemStack record) { setItem(0, record); }
    @Override public ItemStack getRecord() { return getItem(0); }
    @Override public Jukebox getHolder() { return holder; }

    @Override public void setItem(int slot, ItemStack record) {
        checkIndex(slot);
        JukeboxBlockEntity jukebox = (JukeboxBlockEntity) inventory;
        var item = CraftItemStack.asNMSCopy(record);
        jukebox.inventory.set(0, item);
        jukebox.isPlaying = !item.isEmpty();
        jukebox.recordStartTick = jukebox.tickCount;
        if (jukebox.getWorld() != null && jukebox.getWorld().getBlockEntity(jukebox.getPos()) == jukebox) {
            var state = jukebox.getWorld().getBlockState(jukebox.getPos());
            jukebox.getWorld().setBlockState(jukebox.getPos(), state.with(net.minecraft.block.JukeboxBlock.HAS_RECORD, !item.isEmpty()), 3);
            if (item.isEmpty()) jukebox.stopPlaying();
            else jukebox.startPlaying();
        }
        jukebox.markDirty();
    }

    @Override public void clear() { setItem(0, null); }
}
