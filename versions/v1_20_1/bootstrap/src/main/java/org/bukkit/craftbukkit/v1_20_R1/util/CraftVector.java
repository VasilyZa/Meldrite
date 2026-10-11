package org.bukkit.craftbukkit.v1_20_R1.util;

public final class CraftVector {
    private CraftVector() { }
    public static org.bukkit.util.Vector toBukkit(net.minecraft.util.math.Vec3d vector) { return new org.bukkit.util.Vector(vector.x, vector.y, vector.z); }
    public static net.minecraft.util.math.Vec3d toNMS(org.bukkit.util.Vector vector) { return new net.minecraft.util.math.Vec3d(vector.getX(), vector.getY(), vector.getZ()); }
    public static org.bukkit.util.Vector toBukkit(net.minecraft.util.math.BlockPos pos) { return new org.bukkit.util.Vector(pos.getX(), pos.getY(), pos.getZ()); }
    public static net.minecraft.util.math.BlockPos toBlockPos(org.bukkit.util.Vector vector) { return net.minecraft.util.math.BlockPos.ofFloored(vector.getX(), vector.getY(), vector.getZ()); }
}
