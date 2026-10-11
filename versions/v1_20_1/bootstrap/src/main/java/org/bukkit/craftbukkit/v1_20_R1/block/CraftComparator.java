package org.bukkit.craftbukkit.v1_20_R1.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.ComparatorBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class CraftComparator extends CraftBlockEntityState<ComparatorBlockEntity> implements org.bukkit.block.Comparator {
    public CraftComparator(ServerWorld world, BlockPos pos, BlockState state, ComparatorBlockEntity tile, boolean snapshot) { super(world, pos, state, tile, snapshot); }
}
