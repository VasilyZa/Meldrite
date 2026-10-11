package org.bukkit.craftbukkit.map;

import java.util.ArrayList;
import org.bukkit.map.MapCursor;

/** Render result used by the native map packet bridge. */
public class RenderData {
    public final byte[] buffer = new byte[128 * 128];
    public final ArrayList<MapCursor> cursors = new ArrayList<>();
}
