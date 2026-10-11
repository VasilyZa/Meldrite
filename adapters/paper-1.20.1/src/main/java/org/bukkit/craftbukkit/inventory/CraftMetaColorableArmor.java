package org.bukkit.craftbukkit.inventory;

import java.util.Map;
import org.bukkit.inventory.meta.ColorableArmorMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;

@org.bukkit.configuration.serialization.DelegateDeserialization(CraftMetaItem.SerializableMeta.class)
public final class CraftMetaColorableArmor extends CraftMetaLeatherArmor implements ColorableArmorMeta {
    public CraftMetaColorableArmor() { }
    private CraftMetaColorableArmor(CraftMetaColorableArmor source) { super(source); }
    @Override public boolean hasTrim() { return getTrim() != null; }
    @Override public ArmorTrim getTrim() { return CraftMetaArmor.trim(mutableTag()); }
    @Override public void setTrim(ArmorTrim trim) { CraftMetaArmor.trim(mutableTag(), trim); }
    @Override public CraftMetaColorableArmor clone() { return new CraftMetaColorableArmor(this); }
    @Override public Map<String, Object> serialize() { Map<String, Object> result = super.serialize(); result.put("meta-type", "COLORABLE_ARMOR"); if (hasTrim()) result.put("trim", dev.meldrite.paper.facade.ItemNbtData.copy(dev.meldrite.paper.facade.ItemNbtData.compound(mutableTag().get("Trim")))); return result; }
}
