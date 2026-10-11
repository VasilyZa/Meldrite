package org.bukkit.craftbukkit.v1_20_R1.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.bukkit.block.BrewingStand;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventoryBrewer;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventory;
import org.bukkit.inventory.BrewerInventory;

public final class CraftBrewingStand extends CraftContainer implements BrewingStand {
    public CraftBrewingStand(ServerWorld world, BlockPos position, BlockState state,
                             BrewingStandBlockEntity brewingStand, boolean useSnapshot) {
        super(world, position, state, brewingStand, useSnapshot);
    }

    private BrewingStandBlockEntity brewingStand() { return (BrewingStandBlockEntity) snapshot; }
    @Override public BrewingStandBlockEntity getTileEntity() { return (BrewingStandBlockEntity) tileEntity; }
    @Override public BrewingStandBlockEntity getSnapshot() { return brewingStand(); }

    @Override public BrewerInventory getInventory() {
        CraftInventory inventory = (CraftInventory) super.getInventory();
        return new CraftInventoryBrewer(inventory.getInventory(), this);
    }

    @Override public BrewerInventory getSnapshotInventory() {
        return new CraftInventoryBrewer(snapshot, this);
    }

    @Override public int getBrewingTime() { return brewingStand().brewTime; }
    @Override public void setBrewingTime(int time) {
        brewingStand().brewTime = time;
    }
    @Override public int getFuelLevel() { return brewingStand().fuel; }
    @Override public void setFuelLevel(int level) {
        brewingStand().fuel = level;
    }
}
