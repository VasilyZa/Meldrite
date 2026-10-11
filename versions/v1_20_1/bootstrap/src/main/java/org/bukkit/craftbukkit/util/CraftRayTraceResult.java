package org.bukkit.craftbukkit.util;

import net.minecraft.util.hit.HitResult;
import org.bukkit.World;
import org.bukkit.util.RayTraceResult;

/** Legacy package entry point retained for plugins compiled against CraftBukkit. */
public final class CraftRayTraceResult {
    private CraftRayTraceResult() {}

    public static RayTraceResult fromNMS(World world, HitResult hit) {
        return org.bukkit.craftbukkit.v1_20_R1.util.CraftRayTraceResult.fromNMS(world, hit);
    }
}
