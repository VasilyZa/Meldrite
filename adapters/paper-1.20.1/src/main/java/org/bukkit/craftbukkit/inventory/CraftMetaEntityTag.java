package org.bukkit.craftbukkit.inventory;

import java.util.Map;

/** Entity-tag item types share the lossless common metadata operations. */
@org.bukkit.configuration.serialization.DelegateDeserialization(CraftMetaItem.SerializableMeta.class)
public class CraftMetaEntityTag extends CraftMetaItem {
    public CraftMetaEntityTag() { }
    protected CraftMetaEntityTag(CraftMetaEntityTag source) { super(source); }
    @Override public CraftMetaEntityTag clone() { return new CraftMetaEntityTag(this); }
    @Override public Map<String, Object> serialize() { return serialized("ENTITY_TAG"); }
}
