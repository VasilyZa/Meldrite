package org.bukkit.craftbukkit.v1_20_R1;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import net.minecraft.util.math.BlockPos;
import org.bukkit.Location;
import org.bukkit.World;

public class CraftWorldBorder implements org.bukkit.WorldBorder {
    private final CraftWorld world;
    private final net.minecraft.world.border.WorldBorder handle;
    public CraftWorldBorder(CraftWorld world) { this.world = Objects.requireNonNull(world); this.handle = world.getHandle().getWorldBorder(); }
    public CraftWorldBorder(net.minecraft.world.border.WorldBorder handle) { this.world = null; this.handle = Objects.requireNonNull(handle); }
    public net.minecraft.world.border.WorldBorder getHandle() { if (world != null) world.getHandle(); return handle; }
    public boolean isVirtual() { return world == null; }
    private void writable() { if (world != null) world.requireWritable(); }
    @Override public World getWorld() { return world; }
    @Override public void reset() { writable(); getHandle().load(net.minecraft.world.border.WorldBorder.DEFAULT_BORDER); }
    @Override public double getSize() { return getHandle().getSize(); }
    @Override public void setSize(double size) { setSize(size, 0L); }
    @Override public void setSize(double size, long seconds) {
        setSize(Math.min(getMaxSize(), Math.max(1, size)), TimeUnit.SECONDS, Math.min(Long.MAX_VALUE / 1000, Math.max(0, seconds)));
    }
    @Override public void setSize(double size, TimeUnit unit, long time) {
        Objects.requireNonNull(unit, "unit");
        if (!Double.isFinite(size) || size < 1 || size > getMaxSize() || time < 0) throw new IllegalArgumentException("Invalid border size or duration");
        writable();
        if (time > 0) getHandle().interpolateSize(getSize(), size, unit.toMillis(time)); else getHandle().setSize(size);
    }
    @Override public Location getCenter() { return new Location(world, getHandle().getCenterX(), 0, getHandle().getCenterZ()); }
    @Override public void setCenter(double x, double z) {
        if (!Double.isFinite(x) || !Double.isFinite(z) || Math.abs(x) > getMaxCenterCoordinate() || Math.abs(z) > getMaxCenterCoordinate()) throw new IllegalArgumentException("Invalid border center");
        writable(); getHandle().setCenter(x, z);
    }
    @Override public void setCenter(Location center) { Objects.requireNonNull(center, "center"); setCenter(center.getX(), center.getZ()); }
    @Override public double getDamageBuffer() { return getHandle().getSafeZone(); }
    @Override public void setDamageBuffer(double value) { writable(); getHandle().setSafeZone(value); }
    @Override public double getDamageAmount() { return getHandle().getDamagePerBlock(); }
    @Override public void setDamageAmount(double value) { writable(); getHandle().setDamagePerBlock(value); }
    @Override public int getWarningTime() { return getHandle().getWarningTime(); }
    @Override public void setWarningTime(int value) { writable(); getHandle().setWarningTime(value); }
    @Override public int getWarningDistance() { return getHandle().getWarningBlocks(); }
    @Override public void setWarningDistance(int value) { writable(); getHandle().setWarningBlocks(value); }
    @Override public boolean isInside(Location location) {
        Objects.requireNonNull(location, "location").checkFinite();
        return (world == null || world.equals(location.getWorld())) && getHandle().contains(BlockPos.ofFloored(location.getX(), location.getY(), location.getZ()));
    }
    @Override public double getMaxSize() { return net.minecraft.world.border.WorldBorder.STATIC_AREA_SIZE; }
    @Override public double getMaxCenterCoordinate() { return net.minecraft.world.border.WorldBorder.MAX_CENTER_COORDINATES; }
}
