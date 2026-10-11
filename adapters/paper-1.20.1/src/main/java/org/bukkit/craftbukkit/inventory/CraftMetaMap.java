package org.bukkit.craftbukkit.inventory;

import dev.meldrite.paper.facade.ItemNbtData;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

@org.bukkit.configuration.serialization.DelegateDeserialization(CraftMetaItem.SerializableMeta.class)
public final class CraftMetaMap extends CraftMetaItem implements MapMeta {
    public CraftMetaMap() { }
    private CraftMetaMap(CraftMetaMap source) { super(source); }
    private Map<String, Object> display() { return ItemNbtData.compound(mutableTag().get("display")); }
    private void display(String key, Object value) { if (value == null) { if (!display().isEmpty()) display().remove(key); } else mutableCompound("display").put(key, value); }
    @Override public boolean hasMapId() { return mutableTag().get("map") instanceof Number; }
    @Override public int getMapId() { if (!hasMapId()) throw new IllegalStateException("Map ID is absent"); return number("map", 0); }
    @Override public void setMapId(int id) { value("map", id); }
    @Override public boolean hasMapView() { return hasMapId(); }
    @Override public MapView getMapView() { return hasMapId() ? Bukkit.getMap(getMapId()) : null; }
    @Override public void setMapView(MapView view) { value("map", view == null ? null : view.getId()); }
    @Override public boolean isScaling() { return number("map_is_scaling", 0) != 0; }
    @Override public void setScaling(boolean scaling) { value("map_is_scaling", (byte) (scaling ? 1 : 0)); }
    @Override public boolean hasLocationName() { return display().get("LocName") instanceof String; }
    @Override public String getLocationName() { return hasLocationName() ? (String) display().get("LocName") : null; }
    @Override public void setLocationName(String name) { display("LocName", name); }
    @Override public boolean hasColor() { return display().get("MapColor") instanceof Number; }
    @Override public Color getColor() { return hasColor() ? Color.fromRGB(((Number) display().get("MapColor")).intValue()) : null; }
    @Override public void setColor(Color color) { display("MapColor", color == null ? null : color.asRGB()); }
    @Override public CraftMetaMap clone() { return new CraftMetaMap(this); }
    @Override public Map<String, Object> serialize() { Map<String, Object> result = serialized("MAP"); if (hasMapId()) result.put("map-id", getMapId()); if (mutableTag().containsKey("map_is_scaling")) result.put("scaling", isScaling()); if (hasLocationName()) result.put("loc-name", getLocationName()); if (hasColor()) result.put("color", getColor()); return result; }
}
