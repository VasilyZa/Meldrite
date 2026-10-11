package org.bukkit.craftbukkit.v1_20_R1.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventoryLectern;
import org.bukkit.inventory.Inventory;

public final class CraftLectern extends CraftBlockEntityState<LecternBlockEntity> implements org.bukkit.block.Lectern {
    public CraftLectern(ServerWorld world, BlockPos position, BlockState state, LecternBlockEntity tile, boolean useSnapshot) {
        super(world, position, state, tile, useSnapshot);
    }

    @Override public int getPage() { return snapshot.getCurrentPage(); }
    @Override public void setPage(int page) {
        snapshot.currentPage = net.minecraft.util.math.MathHelper.clamp(page, 0, Math.max(0, snapshot.pageCount - 1));
    }
    @Override public Inventory getSnapshotInventory() { return new CraftInventoryLectern(snapshot, this); }
    @Override public Inventory getInventory() {
        LecternBlockEntity current = isPlaced() && world.getBlockEntity(position) instanceof LecternBlockEntity lectern ? lectern : snapshot;
        return new CraftInventoryLectern(current, this);
    }

    @Override public boolean update(boolean force, boolean applyPhysics) {
        if (!super.update(force, applyPhysics)) return false;
        if (!isPlaced()) return true;
        if (world.getBlockEntity(position) instanceof LecternBlockEntity lectern) {
            LecternBlock.setHasBook(null, world, position, world.getBlockState(position), lectern.hasBook());
            LecternBlock.setPowered(world, position, world.getBlockState(position));
        }
        return true;
    }
}
