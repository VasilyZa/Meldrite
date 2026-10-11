package org.bukkit.craftbukkit.v1_20_R1.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.EnchantingTableBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public final class CraftEnchantingTable extends CraftNameableBlockEntityState<EnchantingTableBlockEntity>
        implements org.bukkit.block.EnchantingTable {
    public CraftEnchantingTable(ServerWorld world, BlockPos position, BlockState state,
                                EnchantingTableBlockEntity tile, boolean useSnapshot) {
        super(world, position, state, tile, useSnapshot);
    }

    @Override protected void setNativeCustomName(Text name) { snapshot.setCustomName(name); }
    @Override protected void setLiveCustomName(EnchantingTableBlockEntity tile, Text name) { tile.setCustomName(name); }
}
