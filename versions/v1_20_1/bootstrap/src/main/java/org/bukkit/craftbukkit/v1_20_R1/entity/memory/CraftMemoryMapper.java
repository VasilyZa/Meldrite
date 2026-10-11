package org.bukkit.craftbukkit.v1_20_R1.entity.memory;
import net.minecraft.util.math.GlobalPos;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import dev.meldrite.v1201.compat.NativeEntityExtensions;
public final class CraftMemoryMapper {
    private CraftMemoryMapper() {}
    public static Object fromNms(Object value) { if (value instanceof GlobalPos pos) return fromNms(pos); if (value instanceof Long || value instanceof java.util.UUID || value instanceof Boolean || value instanceof Integer) return value; throw new UnsupportedOperationException("Cannot map native memory: " + value); }
    public static Object toNms(Object value) { if (value instanceof Location location) return toNms(location); if (value == null || value instanceof Long || value instanceof java.util.UUID || value instanceof Boolean || value instanceof Integer) return value; throw new UnsupportedOperationException("Cannot map Bukkit memory: " + value); }
    public static Location fromNms(GlobalPos pos) { var world = ((CraftServer) Bukkit.getServer()).getServer().getWorld(pos.getDimension()); if (world == null) throw new IllegalArgumentException("Memory world is not loaded: " + pos.getDimension()); var block = pos.getPos(); return new Location(NativeEntityExtensions.world(world), block.getX(), block.getY(), block.getZ()); }
    public static GlobalPos toNms(Location location) { if (!(location.getWorld() instanceof CraftWorld world)) throw new IllegalArgumentException("Memory location requires a native world"); return GlobalPos.create(world.getHandle().getRegistryKey(), new net.minecraft.util.math.BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ())); }
}
