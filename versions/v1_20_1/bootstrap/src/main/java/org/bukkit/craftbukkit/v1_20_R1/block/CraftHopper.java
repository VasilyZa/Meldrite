package org.bukkit.craftbukkit.v1_20_R1.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class CraftHopper extends CraftLootable<HopperBlockEntity> implements org.bukkit.block.Hopper {
    public CraftHopper(ServerWorld world, BlockPos pos, BlockState state, HopperBlockEntity tile, boolean snapshot) { super(world, pos, state, tile, snapshot); }
}
