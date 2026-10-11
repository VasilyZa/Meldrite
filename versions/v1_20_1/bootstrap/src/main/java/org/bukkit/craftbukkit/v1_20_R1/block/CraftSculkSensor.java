package org.bukkit.craftbukkit.v1_20_R1.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.SculkSensorBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import dev.meldrite.v1201.compat.NativeSensorRange;

public class CraftSculkSensor<T extends SculkSensorBlockEntity> extends CraftBlockEntityState<T> implements org.bukkit.block.SculkSensor {
    public CraftSculkSensor(ServerWorld world, BlockPos pos, BlockState state, T tile, boolean snapshot) { super(world, pos, state, tile, snapshot); }
    @Override public int getLastVibrationFrequency() { return snapshot.getLastVibrationFrequency(); }
    @Override public void setLastVibrationFrequency(int value) { if (value < 0 || value > 15) throw new IllegalArgumentException("Vibration frequency must be between 0 and 15"); snapshot.setLastVibrationFrequency(value); }
    @Override public int getListenerRange() { return snapshot.getEventListener().getRange(); }
    @Override public void setListenerRange(int value) { if (value <= 0) throw new IllegalArgumentException("Listener range must be positive"); ((NativeSensorRange) snapshot).meldrite$setRange(value); }
}
