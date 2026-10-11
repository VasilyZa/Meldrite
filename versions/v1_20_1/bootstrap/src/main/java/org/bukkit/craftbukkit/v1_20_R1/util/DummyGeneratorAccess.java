package org.bukkit.craftbukkit.v1_20_R1.util;

import java.util.Objects;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;

/** Read/write adapter used by code paths that need a generation-access handle. */
public final class DummyGeneratorAccess {
    private final StructureWorldAccess world;
    public DummyGeneratorAccess(StructureWorldAccess world) { this.world = Objects.requireNonNull(world, "world"); }
    public StructureWorldAccess getHandle() { return world; }
    public BlockState getBlockState(BlockPos pos) { return world.getBlockState(pos); }
    public BlockEntity getBlockEntity(BlockPos pos) { return world.getBlockEntity(pos); }
    public boolean setBlock(BlockPos pos, BlockState state, int flags) { return world.setBlockState(pos, state, flags, 512); }
}
