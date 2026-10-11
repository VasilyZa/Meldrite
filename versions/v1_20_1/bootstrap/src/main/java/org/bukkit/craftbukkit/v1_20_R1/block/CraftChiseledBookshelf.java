package org.bukkit.craftbukkit.v1_20_R1.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.ChiseledBookshelfBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventoryChiseledBookshelf;
import org.bukkit.inventory.ChiseledBookshelfInventory;

public class CraftChiseledBookshelf extends CraftBlockEntityState<ChiseledBookshelfBlockEntity> implements org.bukkit.block.ChiseledBookshelf {
    public CraftChiseledBookshelf(ServerWorld world, BlockPos pos, BlockState state, ChiseledBookshelfBlockEntity tile, boolean snapshot) { super(world, pos, state, tile, snapshot); }
    @Override public int getLastInteractedSlot() { return snapshot.getLastInteractedSlot(); }
    @Override public void setLastInteractedSlot(int slot) { snapshot.lastInteractedSlot = slot; }
    @Override public ChiseledBookshelfInventory getInventory() { return new CraftInventoryChiseledBookshelf(liveTile(), this); }
    @Override public ChiseledBookshelfInventory getSnapshotInventory() { return new CraftInventoryChiseledBookshelf(snapshot, this); }
    @Override public int getSlot(org.bukkit.util.Vector vector) {
        java.util.Objects.requireNonNull(vector, "clickVector").checkFinite();
        var facing = state.get(net.minecraft.state.property.Properties.HORIZONTAL_FACING);
        double x = switch (facing) { case NORTH -> 1 - vector.getX(); case SOUTH -> vector.getX(); case WEST -> vector.getZ(); case EAST -> 1 - vector.getZ(); default -> -1; };
        if (x < 0) return -1;
        return (vector.getY() >= 0.5 ? 0 : 3) + (x < 0.375 ? 0 : x < 0.6875 ? 1 : 2);
    }
    @Override public boolean update(boolean force, boolean physics) {
        for (int i = 0; i < 6; i++) state = state.with(net.minecraft.block.ChiseledBookshelfBlock.SLOT_OCCUPIED_PROPERTIES.get(i), !snapshot.getStack(i).isEmpty());
        return super.update(force, physics);
    }
}
