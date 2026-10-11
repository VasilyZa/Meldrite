package org.bukkit.craftbukkit.v1_20_R1.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.SculkCatalystBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class CraftSculkCatalyst extends CraftBlockEntityState<SculkCatalystBlockEntity> implements org.bukkit.block.SculkCatalyst {
    public CraftSculkCatalyst(ServerWorld world, BlockPos pos, BlockState state, SculkCatalystBlockEntity tile, boolean snapshot) { super(world, pos, state, tile, snapshot); }
    private void bloomAt(BlockPos target, int charge) {
        requirePlaced(); if (charge <= 0) throw new IllegalArgumentException("Charge must be positive");
        var tile = liveTile(); tile.getEventListener().bloom(world, position, tile.getCachedState(), world.random);
        tile.getEventListener().getSpreadManager().spread(target, charge);
    }
    @Override public void bloom(org.bukkit.block.Block block, int charge) { java.util.Objects.requireNonNull(block, "block"); bloomAt(new BlockPos(block.getX(), block.getY(), block.getZ()), charge); }
    @Override public void bloom(io.papermc.paper.math.Position target, int charge) { java.util.Objects.requireNonNull(target, "position"); bloomAt(BlockPos.ofFloored(target.x(), target.y(), target.z()), charge); }
}
