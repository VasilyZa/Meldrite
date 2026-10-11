package org.bukkit.craftbukkit.inventory;

@org.bukkit.configuration.serialization.DelegateDeserialization(CraftMetaItem.SerializableMeta.class)
public final class CraftMetaEnchantedBook extends dev.meldrite.paper.facade.EnchantedBookItemMeta {
    public CraftMetaEnchantedBook() { }
    private CraftMetaEnchantedBook(CraftMetaEnchantedBook source) { super(source); }
    @Override public CraftMetaEnchantedBook clone() { return new CraftMetaEnchantedBook(this); }
}
