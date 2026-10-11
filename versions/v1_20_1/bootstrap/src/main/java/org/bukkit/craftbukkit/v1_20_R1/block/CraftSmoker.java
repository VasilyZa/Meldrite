package org.bukkit.craftbukkit.v1_20_R1.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.SmokerBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class CraftSmoker extends CraftFurnace implements org.bukkit.block.Smoker {
    public CraftSmoker(ServerWorld world, BlockPos position, BlockState state,
                       SmokerBlockEntity furnace, boolean useSnapshot) {
        super(world, position, state, furnace, useSnapshot);
    }
}
