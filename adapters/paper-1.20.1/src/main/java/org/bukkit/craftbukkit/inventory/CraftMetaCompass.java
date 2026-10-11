package org.bukkit.craftbukkit.inventory;

import dev.meldrite.paper.facade.ItemNbtData;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.CompassMeta;

@org.bukkit.configuration.serialization.DelegateDeserialization(CraftMetaItem.SerializableMeta.class)
public final class CraftMetaCompass extends CraftMetaItem implements CompassMeta {
    public CraftMetaCompass() { }
    private CraftMetaCompass(CraftMetaCompass source) { super(source); }
    @Override public boolean hasLodestone() { return mutableTag().get("LodestoneDimension") instanceof String && mutableTag().get("LodestonePos") instanceof Map<?, ?>; }
    @Override public Location getLodestone() {
        if (!hasLodestone()) return null;
        Map<String, Object> position = ItemNbtData.compound(mutableTag().get("LodestonePos"));
        NamespacedKey key = NamespacedKey.fromString((String) mutableTag().get("LodestoneDimension"));
        return new Location(key == null ? null : Bukkit.getWorld(key), coordinate(position, "X"), coordinate(position, "Y"), coordinate(position, "Z"));
    }
    private static int coordinate(Map<String, Object> tag, String key) { return tag.get(key) instanceof Number number ? number.intValue() : 0; }
    @Override public void setLodestone(Location location) {
        if (location == null) { value("LodestoneDimension", null); value("LodestonePos", null); return; }
        if (location.getWorld() == null) throw new IllegalArgumentException("Lodestone world is null");
        value("LodestoneDimension", location.getWorld().getKey().toString());
        value("LodestonePos", new java.util.LinkedHashMap<>(Map.of("X", location.getBlockX(), "Y", location.getBlockY(), "Z", location.getBlockZ())));
    }
    @Override public boolean isLodestoneTracked() { return number("LodestoneTracked", 0) != 0; }
    @Override public void setLodestoneTracked(boolean tracked) { value("LodestoneTracked", (byte) (tracked ? 1 : 0)); }
    @Override public CraftMetaCompass clone() { return new CraftMetaCompass(this); }
    @Override public Map<String, Object> serialize() { return serialized("COMPASS"); }
}
