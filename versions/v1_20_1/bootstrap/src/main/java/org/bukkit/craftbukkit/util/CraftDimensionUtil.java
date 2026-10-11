package org.bukkit.craftbukkit.util;

import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionTypes;

/** Converts a dimension's registry key to its main Bukkit world key. */
public final class CraftDimensionUtil {
    private CraftDimensionUtil() {}

    public static RegistryKey<World> getMainDimensionKey(World world) {
        var dimension = world.getDimensionKey();
        if (dimension == DimensionTypes.OVERWORLD) return World.OVERWORLD;
        if (dimension == DimensionTypes.THE_NETHER) return World.NETHER;
        if (dimension == DimensionTypes.THE_END) return World.END;
        return world.getRegistryKey();
    }
}
