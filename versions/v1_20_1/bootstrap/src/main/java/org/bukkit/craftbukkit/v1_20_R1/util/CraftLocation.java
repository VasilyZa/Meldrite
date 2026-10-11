package org.bukkit.craftbukkit.v1_20_R1.util;

import net.minecraft.util.math.*;
import org.bukkit.Location;
import org.bukkit.World;

public final class CraftLocation {
    private CraftLocation() { }
    public static Location toBukkit(Vec3d pos) { return toBukkit(pos, null); }
    public static Location toBukkit(Vec3d pos, World world) { return toBukkit(pos, world, 0, 0); }
    public static Location toBukkit(Vec3d pos, World world, float yaw, float pitch) { return new Location(world, pos.x, pos.y, pos.z, yaw, pitch); }
    public static Location toBukkit(BlockPos pos) { return toBukkit(pos, (World) null); }
    public static Location toBukkit(BlockPos pos, net.minecraft.world.World world) { return toBukkit(pos, dev.meldrite.v1201.compat.NativeEntityExtensions.world((net.minecraft.server.world.ServerWorld) world)); }
    public static Location toBukkit(BlockPos pos, World world) { return toBukkit(pos, world, 0, 0); }
    public static Location toBukkit(BlockPos pos, World world, float yaw, float pitch) { return new Location(world, pos.getX(), pos.getY(), pos.getZ(), yaw, pitch); }
    public static Location toBukkit(PositionImpl pos) { return toBukkit(pos, null); }
    public static Location toBukkit(PositionImpl pos, World world) { return toBukkit(pos, world, 0, 0); }
    public static Location toBukkit(PositionImpl pos, World world, float yaw, float pitch) { return new Location(world, pos.getX(), pos.getY(), pos.getZ(), yaw, pitch); }
    public static BlockPos toBlockPosition(Location location) { return new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()); }
    public static PositionImpl toPosition(Location location) { return new PositionImpl(location.getX(), location.getY(), location.getZ()); }
    public static Vec3d toVec3D(Location location) { return new Vec3d(location.getX(), location.getY(), location.getZ()); }
}
