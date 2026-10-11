package org.bukkit.craftbukkit.v1_20_R1.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.JigsawBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class CraftJigsaw extends CraftBlockEntityState<JigsawBlockEntity> implements org.bukkit.block.Jigsaw {
    public CraftJigsaw(ServerWorld world, BlockPos pos, BlockState state, JigsawBlockEntity tile, boolean snapshot) { super(world, pos, state, tile, snapshot); }
}
