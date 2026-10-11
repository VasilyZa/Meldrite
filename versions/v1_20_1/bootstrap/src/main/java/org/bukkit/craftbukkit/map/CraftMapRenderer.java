package org.bukkit.craftbukkit.map;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

/** Renderer that projects the native map pixel buffer into a Bukkit canvas. */
public class CraftMapRenderer extends MapRenderer {
    public CraftMapRenderer() { super(false); }
    @Override public void render(MapView map, MapCanvas canvas, Player player) { if (map instanceof CraftMapView view) for (int y = 0; y < 128; y++) for (int x = 0; x < 128; x++) canvas.setPixel(x, y, view.pixel(x, y)); }
}
