package org.bukkit.craftbukkit.v1_20_R1.block;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

/** Captured block state facade used by tree and structure generation code. */
public final class CapturedBlockState extends CraftBlockState {
    private final boolean treeBlock;
    public CapturedBlockState(org.bukkit.block.Block block, int flags, boolean treeBlock) {
        super(block, flags);
        this.treeBlock = treeBlock;
    }
    public boolean isTreeBlock() { return treeBlock; }
    public static CapturedBlockState getBlockState(ServerWorld world, BlockPos position, int flags) {
        return new CapturedBlockState(CraftBlock.at(world, position), flags, false);
    }
    public static CapturedBlockState getTreeBlockState(ServerWorld world, BlockPos position, int flags) {
        return new CapturedBlockState(CraftBlock.at(world, position), flags, true);
    }
    public static CapturedBlockState getBlockState(net.minecraft.world.WorldAccess world, BlockPos position, int flags) {
        if (!(world instanceof ServerWorld serverWorld)) throw new IllegalArgumentException("Captured Bukkit block states require a live server world");
        return getBlockState(serverWorld, position, flags);
    }
    public static CapturedBlockState getBlockState(net.minecraft.world.World world, BlockPos position, int flags) {
        if (!(world instanceof ServerWorld serverWorld)) throw new IllegalArgumentException("Captured Bukkit block states require a live server world");
        return getBlockState(serverWorld, position, flags);
    }
    public static CapturedBlockState getTreeBlockState(net.minecraft.world.WorldAccess world, BlockPos position, int flags) {
        if (!(world instanceof ServerWorld serverWorld)) throw new IllegalArgumentException("Captured Bukkit block states require a live server world");
        return getTreeBlockState(serverWorld, position, flags);
    }
    public static CapturedBlockState getTreeBlockState(net.minecraft.world.World world, BlockPos position, int flags) {
        if (!(world instanceof ServerWorld serverWorld)) throw new IllegalArgumentException("Captured Bukkit block states require a live server world");
        return getTreeBlockState(serverWorld, position, flags);
    }
}
