package org.bukkit.craftbukkit.v1_20_R1.block;

import dev.meldrite.v1201.compat.NativeViewerState;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class CraftEnderChest extends CraftBlockEntityState<EnderChestBlockEntity> implements org.bukkit.block.EnderChest {
    public CraftEnderChest(ServerWorld world, BlockPos pos, BlockState state, EnderChestBlockEntity tile, boolean snapshot) { super(world, pos, state, tile, snapshot); }
    private NativeViewerState lid() { return (NativeViewerState) liveTile().stateManager; }
    @Override public void open() { requirePlaced(); lid().meldrite$setOpen(world, position, world.getBlockState(position), true); }
    @Override public void close() { requirePlaced(); lid().meldrite$setOpen(world, position, world.getBlockState(position), false); }
    @Override public boolean isOpen() { return lid().meldrite$isOpen(); }
}
