package org.bukkit.craftbukkit.v1_20_R1.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.bukkit.DyeColor;

public class CraftShulkerBox extends CraftLootable<ShulkerBoxBlockEntity> implements org.bukkit.block.ShulkerBox {
    public CraftShulkerBox(ServerWorld world, BlockPos pos, BlockState state, ShulkerBoxBlockEntity tile, boolean snapshot) { super(world, pos, state, tile, snapshot); }
    @Override public DyeColor getColor() { var color = getSnapshot().getColor(); return color == null ? null : DyeColor.getByWoolData((byte) color.getId()); }
    private void setOpen(boolean value) {
        requirePlaced(); var tile = liveTile();
        var access = (dev.meldrite.v1201.compat.NativeShulkerLid) tile;
        if (access.meldrite$isOpen() == value) return;
        access.meldrite$setOpen(value);
        world.addSyncedBlockEvent(position, tile.getCachedState().getBlock(), 1, tile.viewerCount + (value ? 1 : 0));
        world.playSound(null, position, value ? net.minecraft.sound.SoundEvents.BLOCK_SHULKER_BOX_OPEN : net.minecraft.sound.SoundEvents.BLOCK_SHULKER_BOX_CLOSE,
                net.minecraft.sound.SoundCategory.BLOCKS, 0.5f, world.random.nextFloat() * 0.1f + 0.9f);
    }
    @Override public void open() { setOpen(true); }
    @Override public void close() { setOpen(false); }
    @Override public boolean isOpen() { return ((dev.meldrite.v1201.compat.NativeShulkerLid) liveTile()).meldrite$isOpen(); }
}
