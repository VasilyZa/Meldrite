package org.bukkit.craftbukkit.v1_20_R1.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.SculkShriekerBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class CraftSculkShrieker extends CraftBlockEntityState<SculkShriekerBlockEntity> implements org.bukkit.block.SculkShrieker {
    public CraftSculkShrieker(ServerWorld world, BlockPos pos, BlockState state, SculkShriekerBlockEntity tile, boolean snapshot) { super(world, pos, state, tile, snapshot); }
    @Override public int getWarningLevel() { return snapshot.warningLevel; }
    @Override public void setWarningLevel(int value) { snapshot.warningLevel = value; }
    @Override public void tryShriek(org.bukkit.entity.Player player) { requirePlaced(); liveTile().shriek(world, player == null ? null : ((org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer) player).getHandle()); }
}
