package org.bukkit.craftbukkit.v1_20_R1.inventory;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

/** Historical inventory package alias retained by CraftBukkit plugins. */
public class CraftContainer extends org.bukkit.craftbukkit.v1_20_R1.block.CraftContainer {
    public CraftContainer(ServerWorld world, BlockPos position, BlockState state, LockableContainerBlockEntity container, boolean useSnapshot) {
        super(world, position, state, container, useSnapshot);
    }
}
