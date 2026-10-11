package org.bukkit.craftbukkit.v1_20_R1.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlastFurnaceBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class CraftBlastFurnace extends CraftFurnace implements org.bukkit.block.BlastFurnace {
    public CraftBlastFurnace(ServerWorld world, BlockPos position, BlockState state,
                             BlastFurnaceBlockEntity furnace, boolean useSnapshot) {
        super(world, position, state, furnace, useSnapshot);
    }
}
