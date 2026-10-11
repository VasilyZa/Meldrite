package org.bukkit.craftbukkit.v1_20_R1.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.CalibratedSculkSensorBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class CraftCalibratedSculkSensor extends CraftSculkSensor<CalibratedSculkSensorBlockEntity> implements org.bukkit.block.CalibratedSculkSensor {
    public CraftCalibratedSculkSensor(ServerWorld world, BlockPos pos, BlockState state, CalibratedSculkSensorBlockEntity tile, boolean snapshot) { super(world, pos, state, tile, snapshot); }
}
