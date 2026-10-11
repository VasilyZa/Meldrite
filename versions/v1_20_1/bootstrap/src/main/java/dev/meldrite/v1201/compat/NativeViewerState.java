package dev.meldrite.v1201.compat;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface NativeViewerState {
    boolean meldrite$isOpen();
    void meldrite$setOpen(World world, BlockPos position, BlockState state, boolean value);
}
