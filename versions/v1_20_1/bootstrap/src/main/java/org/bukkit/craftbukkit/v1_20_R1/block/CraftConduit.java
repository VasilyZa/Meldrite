package org.bukkit.craftbukkit.v1_20_R1.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.ConduitBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class CraftConduit extends CraftBlockEntityState<ConduitBlockEntity> implements org.bukkit.block.Conduit {
    public CraftConduit(ServerWorld world, BlockPos pos, BlockState state, ConduitBlockEntity tile, boolean snapshot) { super(world, pos, state, tile, snapshot); }
}
