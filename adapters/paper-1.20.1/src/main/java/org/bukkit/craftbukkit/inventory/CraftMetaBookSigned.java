package org.bukkit.craftbukkit.inventory;

@org.bukkit.configuration.serialization.DelegateDeserialization(CraftMetaItem.SerializableMeta.class)
public final class CraftMetaBookSigned extends CraftMetaBook {
    public CraftMetaBookSigned() { super(true); }
    private CraftMetaBookSigned(CraftMetaBookSigned source) { super(source); }
    @Override public CraftMetaBookSigned clone() { return new CraftMetaBookSigned(this); }
}
