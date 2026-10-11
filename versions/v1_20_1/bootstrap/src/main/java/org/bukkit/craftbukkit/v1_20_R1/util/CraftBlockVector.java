package org.bukkit.craftbukkit.v1_20_R1.util;

public final class CraftBlockVector {
    private CraftBlockVector() { }
    public static net.minecraft.util.math.BlockPos toBlockPosition(org.bukkit.util.BlockVector vector) { return new net.minecraft.util.math.BlockPos(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ()); }
    public static org.bukkit.util.BlockVector toBukkit(net.minecraft.util.math.Vec3i vector) { return new org.bukkit.util.BlockVector(vector.getX(), vector.getY(), vector.getZ()); }
}
