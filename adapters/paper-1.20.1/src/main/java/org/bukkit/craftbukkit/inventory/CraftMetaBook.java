package org.bukkit.craftbukkit.inventory;

@org.bukkit.configuration.serialization.DelegateDeserialization(CraftMetaItem.SerializableMeta.class)
public class CraftMetaBook extends dev.meldrite.paper.facade.BookItemMeta {
    public CraftMetaBook() { super(false); }
    protected CraftMetaBook(boolean written) { super(written); }
    protected CraftMetaBook(CraftMetaBook source) { super(source); }
    @Override public CraftMetaBook clone() { return new CraftMetaBook(this); }
}
