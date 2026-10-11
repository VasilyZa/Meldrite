package org.bukkit.craftbukkit.inventory;

import dev.meldrite.paper.facade.ItemNbtData;
import java.util.Map;
import com.destroystokyo.paper.inventory.meta.ArmorStandMeta;

@org.bukkit.configuration.serialization.DelegateDeserialization(CraftMetaItem.SerializableMeta.class)
public final class CraftMetaArmorStand extends CraftMetaItem implements ArmorStandMeta {
    public CraftMetaArmorStand() { }
    private CraftMetaArmorStand(CraftMetaArmorStand source) { super(source); }
    private boolean flag(String key) { Object value = ItemNbtData.compound(mutableTag().get("EntityTag")).get(key); return value instanceof Number number && number.byteValue() != 0; }
    private void flag(String key, boolean value) { mutableCompound("EntityTag").put(key, (byte) (value ? 1 : 0)); }
    @Override public boolean isInvisible() { return flag("Invisible"); }
    @Override public boolean hasNoBasePlate() { return flag("NoBasePlate"); }
    @Override public boolean shouldShowArms() { return flag("ShowArms"); }
    @Override public boolean isSmall() { return flag("Small"); }
    @Override public boolean isMarker() { return flag("Marker"); }
    @Override public void setInvisible(boolean value) { flag("Invisible", value); }
    @Override public void setNoBasePlate(boolean value) { flag("NoBasePlate", value); }
    @Override public void setShowArms(boolean value) { flag("ShowArms", value); }
    @Override public void setSmall(boolean value) { flag("Small", value); }
    @Override public void setMarker(boolean value) { flag("Marker", value); }
    @Override public CraftMetaArmorStand clone() { return new CraftMetaArmorStand(this); }
    @Override public Map<String, Object> serialize() { return serialized("ARMOR_STAND"); }
}
