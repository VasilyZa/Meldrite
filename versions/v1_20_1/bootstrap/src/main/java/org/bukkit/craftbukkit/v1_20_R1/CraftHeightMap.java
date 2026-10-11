package org.bukkit.craftbukkit.v1_20_R1;

public final class CraftHeightMap {
    private CraftHeightMap() { }
    public static net.minecraft.world.Heightmap.Type toNMS(org.bukkit.HeightMap type) { return net.minecraft.world.Heightmap.Type.valueOf(type.name()); }
    public static org.bukkit.HeightMap fromNMS(net.minecraft.world.Heightmap.Type type) { return org.bukkit.HeightMap.valueOf(type.name()); }
}
