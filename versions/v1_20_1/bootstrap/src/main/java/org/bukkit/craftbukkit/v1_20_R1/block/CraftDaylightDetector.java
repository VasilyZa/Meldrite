package org.bukkit.craftbukkit.v1_20_R1.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.DaylightDetectorBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class CraftDaylightDetector extends CraftBlockEntityState<DaylightDetectorBlockEntity> implements org.bukkit.block.DaylightDetector {
    public CraftDaylightDetector(ServerWorld world, BlockPos pos, BlockState state, DaylightDetectorBlockEntity tile, boolean snapshot) { super(world, pos, state, tile, snapshot); }
}
