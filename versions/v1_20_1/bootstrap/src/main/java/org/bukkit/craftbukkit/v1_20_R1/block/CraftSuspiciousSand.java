package org.bukkit.craftbukkit.v1_20_R1.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BrushableBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class CraftSuspiciousSand extends CraftBrushableBlock implements org.bukkit.block.SuspiciousSand {
    public CraftSuspiciousSand(ServerWorld world, BlockPos pos, BlockState state, BrushableBlockEntity tile, boolean snapshot) { super(world, pos, state, tile, snapshot); }
}
