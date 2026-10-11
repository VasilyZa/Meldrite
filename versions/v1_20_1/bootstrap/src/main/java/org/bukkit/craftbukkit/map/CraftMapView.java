package org.bukkit.craftbukkit.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

/** Bukkit map view with a native-compatible state surface and real renderer pipeline. */
public class CraftMapView implements MapView {
    private final int id;
    private final List<MapRenderer> renderers = new ArrayList<>();
    private final Map<MapRenderer, Map<Player, CraftMapCanvas>> canvases = new IdentityHashMap<>();
    private final byte[] pixels = new byte[128 * 128];
    private World world;
    private int centerX;
    private int centerZ;
    private Scale scale = Scale.NORMAL;
    private boolean trackingPosition = true;
    private boolean unlimitedTracking;
    private boolean locked;
    public CraftMapView(int id) { this(id, null); }
    public CraftMapView(int id, World world) { this.id = id; this.world = world; Arrays.fill(pixels, MapPaletteCompat.transparent()); addRenderer(new CraftMapRenderer()); }
    public CraftMapView(net.minecraft.item.map.MapState ignored, int id) { this(id); }
    @Override public int getId() { return id; }
    @Override public boolean isVirtual() { return renderers.stream().noneMatch(CraftMapRenderer.class::isInstance); }
    @Override public Scale getScale() { return scale; }
    @Override public void setScale(Scale scale) { this.scale = java.util.Objects.requireNonNull(scale); }
    @Override public int getCenterX() { return centerX; }
    @Override public int getCenterZ() { return centerZ; }
    @Override public void setCenterX(int value) { centerX = value; }
    @Override public void setCenterZ(int value) { centerZ = value; }
    @Override public World getWorld() { return world; }
    @Override public void setWorld(World world) { this.world = world; }
    @Override public List<MapRenderer> getRenderers() { return List.copyOf(renderers); }
    @Override public void addRenderer(MapRenderer renderer) { if (renderer == null || renderers.contains(renderer)) return; renderers.add(renderer); canvases.put(renderer, new IdentityHashMap<>()); renderer.initialize(this); }
    @Override public boolean removeRenderer(MapRenderer renderer) { canvases.remove(renderer); return renderers.remove(renderer); }
    @Override public boolean isTrackingPosition() { return trackingPosition; }
    @Override public void setTrackingPosition(boolean value) { trackingPosition = value; }
    @Override public boolean isUnlimitedTracking() { return unlimitedTracking; }
    @Override public void setUnlimitedTracking(boolean value) { unlimitedTracking = value; }
    @Override public boolean isLocked() { return locked; }
    @Override public void setLocked(boolean value) { locked = value; }
    byte pixel(int x, int y) { return x >= 0 && x < 128 && y >= 0 && y < 128 ? pixels[y * 128 + x] : 0; }
    void pixel(int x, int y, byte value) { if (x >= 0 && x < 128 && y >= 0 && y < 128) pixels[y * 128 + x] = value; }
    public RenderData render(Player player) { RenderData data = new RenderData(); System.arraycopy(pixels, 0, data.buffer, 0, pixels.length); for (MapRenderer renderer : renderers) { Map<Player, CraftMapCanvas> byPlayer = canvases.get(renderer); Player key = renderer.isContextual() ? player : null; CraftMapCanvas canvas = byPlayer.computeIfAbsent(key, ignored -> new CraftMapCanvas(this)); canvas.setBase(data.buffer); renderer.render(this, canvas, player); System.arraycopy(canvas.getBuffer(), 0, data.buffer, 0, data.buffer.length); for (int i = 0; i < canvas.getCursors().size(); i++) data.cursors.add(canvas.getCursors().getCursor(i)); } System.arraycopy(data.buffer, 0, pixels, 0, pixels.length); return data; }
    private static final class MapPaletteCompat { static byte transparent() { return org.bukkit.map.MapPalette.TRANSPARENT; } }
}
