package org.bukkit.craftbukkit.v1_20_R1.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.DropperBlock;
import net.minecraft.block.entity.DropperBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class CraftDropper extends CraftLootable<DropperBlockEntity> implements org.bukkit.block.Dropper {
    public CraftDropper(ServerWorld world, BlockPos pos, BlockState state, DropperBlockEntity tile, boolean snapshot) { super(world, pos, state, tile, snapshot); }
    @Override public void drop() { requirePlaced(); if (world.getBlockState(position).getBlock() instanceof DropperBlock block) block.dispense(world, position); }
}
