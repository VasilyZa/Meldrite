package org.bukkit.craftbukkit.v1_20_R1.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.bukkit.DyeColor;

public final class CraftBed extends CraftBlockEntityState<BedBlockEntity> implements org.bukkit.block.Bed {
    public CraftBed(ServerWorld world, BlockPos position, BlockState state, BedBlockEntity tile, boolean useSnapshot) {
        super(world, position, state, tile, useSnapshot);
    }

    @Override public DyeColor getColor() { return DyeColor.valueOf(snapshot.getColor().name()); }
    @Override public void setColor(DyeColor color) { throw new UnsupportedOperationException("Must set block type to appropriate bed colour"); }
}
