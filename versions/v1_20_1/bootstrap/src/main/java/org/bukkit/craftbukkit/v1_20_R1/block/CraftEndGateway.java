package org.bukkit.craftbukkit.v1_20_R1.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.EndGatewayBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class CraftEndGateway extends CraftBlockEntityState<EndGatewayBlockEntity> implements org.bukkit.block.EndGateway {
    public CraftEndGateway(ServerWorld world, BlockPos pos, BlockState state, EndGatewayBlockEntity tile, boolean snapshot) { super(world, pos, state, tile, snapshot); }
    @Override public org.bukkit.Location getExitLocation() { var p = snapshot.exitPortalPos; return p == null ? null : new org.bukkit.Location(isPlaced() ? getWorld() : null, p.getX(), p.getY(), p.getZ()); }
    @Override public void setExitLocation(org.bukkit.Location location) {
        if (location != null && !java.util.Objects.equals(location.getWorld(), isPlaced() ? getWorld() : null)) throw new IllegalArgumentException("Exit location must be in the same world");
        snapshot.exitPortalPos = location == null ? null : new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
    @Override public boolean isExactTeleport() { return snapshot.exactTeleport; }
    @Override public void setExactTeleport(boolean value) { snapshot.exactTeleport = value; }
    @Override public long getAge() { return snapshot.age; }
    @Override public void setAge(long value) { snapshot.age = value; }
    @Override public boolean update(boolean force, boolean physics) {
        if (!super.update(force, physics)) return false;
        if (world.getBlockEntity(position) instanceof EndGatewayBlockEntity live) live.exitPortalPos = snapshot.exitPortalPos;
        return true;
    }
}
