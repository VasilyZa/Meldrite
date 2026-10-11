package org.bukkit.craftbukkit.v1_20_R1.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class CraftMovingPiston extends CraftBlockEntityState<PistonBlockEntity> implements io.papermc.paper.block.MovingPiston {
    public CraftMovingPiston(ServerWorld world, BlockPos pos, BlockState state, PistonBlockEntity tile, boolean snapshot) { super(world, pos, state, tile, snapshot); }
    @Override public org.bukkit.block.data.BlockData getMovingBlock() { return org.bukkit.craftbukkit.v1_20_R1.block.data.CraftBlockData.fromData(tileEntity.getPushedBlock()); }
    @Override public org.bukkit.block.BlockFace getDirection() { return CraftBlock.notchToBlockFace(tileEntity.getFacing()); }
    @Override public boolean isExtending() { return tileEntity.isExtending(); }
    @Override public boolean isPistonHead() { return tileEntity.isSource(); }
}
