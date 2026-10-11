package org.bukkit.craftbukkit.v1_20_R1.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.EndPortalBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class CraftEndPortal extends CraftBlockEntityState<EndPortalBlockEntity> {
    public CraftEndPortal(ServerWorld world, BlockPos pos, BlockState state, EndPortalBlockEntity tile, boolean snapshot) { super(world, pos, state, tile, snapshot); }
}
