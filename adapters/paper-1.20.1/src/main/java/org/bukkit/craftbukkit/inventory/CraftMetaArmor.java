package org.bukkit.craftbukkit.inventory;

import dev.meldrite.paper.facade.ItemNbtData;
import dev.meldrite.paper.facade.ItemMetaState;
import java.util.Map;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;

@org.bukkit.configuration.serialization.DelegateDeserialization(CraftMetaItem.SerializableMeta.class)
public class CraftMetaArmor extends CraftMetaItem implements ArmorMeta {
    public CraftMetaArmor() { }
    protected CraftMetaArmor(ItemMetaState source) { super(source); }
    static ArmorTrim trim(Map<String, Object> tag) {
        Map<String, Object> trim = ItemNbtData.compound(tag.get("Trim"));
        NamespacedKey material = trim.get("material") instanceof String text ? NamespacedKey.fromString(text) : null;
        NamespacedKey pattern = trim.get("pattern") instanceof String text ? NamespacedKey.fromString(text) : null;
        var nativeMaterial = material == null ? null : Registry.TRIM_MATERIAL.get(material);
        var nativePattern = pattern == null ? null : Registry.TRIM_PATTERN.get(pattern);
        return nativeMaterial == null || nativePattern == null ? null : new ArmorTrim(nativeMaterial, nativePattern);
    }
    static void trim(Map<String, Object> tag, ArmorTrim trim) {
        if (trim == null) tag.remove("Trim");
        else { Map<String, Object> value = ItemNbtData.copy(ItemNbtData.compound(tag.get("Trim"))); value.put("material", trim.getMaterial().getKey().toString()); value.put("pattern", trim.getPattern().getKey().toString()); tag.put("Trim", value); }
    }
    @Override public boolean hasTrim() { return getTrim() != null; }
    @Override public ArmorTrim getTrim() { return trim(mutableTag()); }
    @Override public void setTrim(ArmorTrim trim) { trim(mutableTag(), trim); }
    @Override public CraftMetaArmor clone() { return new CraftMetaArmor(this); }
    @Override public Map<String, Object> serialize() { Map<String, Object> result = serialized("ARMOR"); if (hasTrim()) result.put("trim", ItemNbtData.copy(ItemNbtData.compound(mutableTag().get("Trim")))); return result; }
}
