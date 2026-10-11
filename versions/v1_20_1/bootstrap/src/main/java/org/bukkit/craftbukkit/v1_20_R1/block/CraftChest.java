package org.bukkit.craftbukkit.v1_20_R1.block;

import dev.meldrite.v1201.compat.NativeViewerState;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventory;

public class CraftChest extends CraftLootable<ChestBlockEntity> implements org.bukkit.block.Chest {
    public CraftChest(ServerWorld world, BlockPos pos, BlockState state, ChestBlockEntity tile, boolean snapshot) { super(world, pos, state, tile, snapshot); }
    @Override public org.bukkit.inventory.Inventory getBlockInventory() { return new CraftInventory(liveTile(), this); }
    private NativeViewerState lid() { return (NativeViewerState) liveTile().stateManager; }
    @Override public void open() { requirePlaced(); lid().meldrite$setOpen(world, position, world.getBlockState(position), true); }
    @Override public void close() { requirePlaced(); lid().meldrite$setOpen(world, position, world.getBlockState(position), false); }
    @Override public boolean isOpen() { return lid().meldrite$isOpen(); }
}
