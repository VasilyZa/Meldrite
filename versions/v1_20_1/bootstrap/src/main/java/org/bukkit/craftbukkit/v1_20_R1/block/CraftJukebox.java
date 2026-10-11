package org.bukkit.craftbukkit.v1_20_R1.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventoryJukebox;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.JukeboxInventory;

public final class CraftJukebox extends CraftBlockEntityState<JukeboxBlockEntity> implements org.bukkit.block.Jukebox {
    public CraftJukebox(ServerWorld world, BlockPos position, BlockState state, JukeboxBlockEntity tile, boolean useSnapshot) {
        super(world, position, state, tile, useSnapshot);
    }

    @Override public Material getPlaying() { return getRecord().getType(); }
    @Override public void setPlaying(Material material) { setRecord(material == null ? null : new ItemStack(material)); }
    @Override public boolean hasRecord() { return state.get(JukeboxBlock.HAS_RECORD) && !snapshot.getStack().isEmpty(); }
    @Override public ItemStack getRecord() { return CraftItemStack.asBukkitCopy(snapshot.getStack()); }
    @Override public void setRecord(ItemStack record) {
        var item = CraftItemStack.asNMSCopy(record);
        snapshot.inventory.set(0, item);
        snapshot.recordStartTick = snapshot.tickCount;
        snapshot.isPlaying = !item.isEmpty();
        state = state.with(JukeboxBlock.HAS_RECORD, !item.isEmpty());
    }

    private JukeboxBlockEntity live() {
        if (!isPlaced()) return null;
        return world.getBlockEntity(position) instanceof JukeboxBlockEntity jukebox ? jukebox : null;
    }

    @Override public boolean isPlaying() { JukeboxBlockEntity live = live(); return live != null && live.isPlayingRecord(); }
    @Override public boolean startPlaying() {
        JukeboxBlockEntity live = live();
        if (live == null || !(live.getStack().getItem() instanceof MusicDiscItem) || live.isPlayingRecord()) return false;
        live.startPlaying();
        return true;
    }
    @Override public void stopPlaying() { JukeboxBlockEntity live = live(); if (live != null) live.stopPlaying(); }
    @Override public boolean eject() {
        JukeboxBlockEntity live = live();
        if (live == null || live.getStack().isEmpty()) return false;
        live.dropRecord();
        return true;
    }
    @Override public JukeboxInventory getSnapshotInventory() { return new CraftInventoryJukebox(snapshot, this); }
    @Override public JukeboxInventory getInventory() { JukeboxBlockEntity live = live(); return new CraftInventoryJukebox(live == null ? snapshot : live, this); }
    @Override public boolean update(boolean force, boolean applyPhysics) {
        state = state.with(JukeboxBlock.HAS_RECORD, !snapshot.getStack().isEmpty());
        if (!super.update(force, applyPhysics)) return false;
        JukeboxBlockEntity live = live();
        if (live != null) {
            live.inventory.set(0, snapshot.getStack().copy());
            if (live.getStack().getItem() instanceof MusicDiscItem) live.startPlaying();
            else live.stopPlaying();
        }
        return true;
    }
}
