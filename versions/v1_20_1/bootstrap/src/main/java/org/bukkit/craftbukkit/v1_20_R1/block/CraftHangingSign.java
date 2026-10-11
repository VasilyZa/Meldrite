package org.bukkit.craftbukkit.v1_20_R1.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.HangingSignBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class CraftHangingSign extends CraftSign implements org.bukkit.block.HangingSign {
    public CraftHangingSign(ServerWorld world, BlockPos pos, BlockState state, HangingSignBlockEntity tile, boolean snapshot) { super(world, pos, state, tile, snapshot); }
    @Override public HangingSignBlockEntity getTileEntity() { return (HangingSignBlockEntity) super.getTileEntity(); }
    @Override public HangingSignBlockEntity getSnapshot() { return (HangingSignBlockEntity) super.getSnapshot(); }
}
