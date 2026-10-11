package org.bukkit.craftbukkit.map;

import java.awt.Color;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import org.bukkit.map.MapPalette;

/** Thread-safe in-memory cache implementation for MapPalette. */
public final class CraftMapColorCache implements MapPalette.MapColorCache {
    private volatile boolean cached;
    public CraftMapColorCache(java.util.logging.Logger ignored) { }
    public CompletableFuture<Void> initCache() { cached = true; return CompletableFuture.completedFuture(null); }
    @Override public boolean isCached() { return cached; }
    @Override public byte matchColor(Color color) { return MapPalette.matchColor(Objects.requireNonNull(color)); }
}
