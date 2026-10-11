package org.bukkit.craftbukkit.v1_20_R1.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BellBlock;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class CraftBell extends CraftBlockEntityState<BellBlockEntity> implements org.bukkit.block.Bell {
    public CraftBell(ServerWorld world, BlockPos pos, BlockState state, BellBlockEntity tile, boolean snapshot) { super(world, pos, state, tile, snapshot); }
    @Override public boolean ring(org.bukkit.entity.Entity entity, org.bukkit.block.BlockFace face) {
        if (face != null && !face.isCartesian()) throw new IllegalArgumentException("Bell direction must be cartesian: " + face);
        requirePlaced(); if (!(world.getBlockEntity(position) instanceof BellBlockEntity)) return false;
        return ((BellBlock) Blocks.BELL).ring(entity == null ? null : ((org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity) entity).getHandle(), world, position, face == null ? null : CraftBlock.blockFaceToNotch(face));
    }
    @Override public boolean ring(org.bukkit.entity.Entity entity) { return ring(entity, null); }
    @Override public boolean ring(org.bukkit.block.BlockFace face) { return ring(null, face); }
    @Override public boolean ring() { return ring(null, null); }
    @Override public boolean isShaking() { return snapshot.ringing; }
    @Override public int getShakingTicks() { return snapshot.ringTicks; }
    @Override public boolean isResonating() { return snapshot.resonating; }
    @Override public int getResonatingTicks() { return snapshot.resonating ? snapshot.ringTicks : 0; }
}
