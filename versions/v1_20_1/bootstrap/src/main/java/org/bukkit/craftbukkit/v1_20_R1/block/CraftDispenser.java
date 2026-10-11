package org.bukkit.craftbukkit.v1_20_R1.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class CraftDispenser extends CraftLootable<DispenserBlockEntity> implements org.bukkit.block.Dispenser {
    public CraftDispenser(ServerWorld world, BlockPos pos, BlockState state, DispenserBlockEntity tile, boolean snapshot) { super(world, pos, state, tile, snapshot); }
    @Override public org.bukkit.projectiles.BlockProjectileSource getBlockProjectileSource() {
        return world.getBlockEntity(position) instanceof DispenserBlockEntity dispenser && !(dispenser instanceof net.minecraft.block.entity.DropperBlockEntity)
                ? new org.bukkit.craftbukkit.v1_20_R1.projectiles.CraftBlockProjectileSource(dispenser) : null;
    }
    @Override public boolean dispense() {
        requirePlaced();
        if (!(world.getBlockState(position).getBlock() instanceof DispenserBlock block)
                || block instanceof net.minecraft.block.DropperBlock) return false;
        block.dispense(world, position); return true;
    }
}
