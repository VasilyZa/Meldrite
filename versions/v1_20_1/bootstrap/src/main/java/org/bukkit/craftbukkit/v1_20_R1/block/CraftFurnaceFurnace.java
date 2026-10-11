package org.bukkit.craftbukkit.v1_20_R1.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class CraftFurnaceFurnace extends CraftFurnace {
    public CraftFurnaceFurnace(ServerWorld world, BlockPos pos, BlockState state, FurnaceBlockEntity tile, boolean snapshot) { super(world, pos, state, tile, snapshot); }
    @Override public FurnaceBlockEntity getTileEntity() { return (FurnaceBlockEntity) super.getTileEntity(); }
    @Override public FurnaceBlockEntity getSnapshot() { return (FurnaceBlockEntity) super.getSnapshot(); }
}
