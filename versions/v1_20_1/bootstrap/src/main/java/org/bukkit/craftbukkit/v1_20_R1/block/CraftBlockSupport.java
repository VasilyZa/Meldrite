package org.bukkit.craftbukkit.v1_20_R1.block;

public final class CraftBlockSupport {
    private CraftBlockSupport() { }
    public static org.bukkit.block.BlockSupport toBukkit(net.minecraft.block.SideShapeType type) { return org.bukkit.block.BlockSupport.valueOf(type.name()); }
    public static net.minecraft.block.SideShapeType toNMS(org.bukkit.block.BlockSupport type) { return net.minecraft.block.SideShapeType.valueOf(type.name()); }
}
