package org.bukkit.craftbukkit.v1_20_R1;

public final class CraftFluidCollisionMode {
    private CraftFluidCollisionMode() { }
    public static net.minecraft.world.RaycastContext.FluidHandling toNMS(org.bukkit.FluidCollisionMode mode) { return mode == null ? null : switch (mode) { case ALWAYS -> net.minecraft.world.RaycastContext.FluidHandling.ANY; case SOURCE_ONLY -> net.minecraft.world.RaycastContext.FluidHandling.SOURCE_ONLY; case NEVER -> net.minecraft.world.RaycastContext.FluidHandling.NONE; }; }
}
