package org.bukkit.craftbukkit.inventory;

@org.bukkit.configuration.serialization.DelegateDeserialization(CraftMetaItem.SerializableMeta.class)
public final class CraftMetaPotion extends dev.meldrite.paper.facade.PotionItemMeta {
    public CraftMetaPotion() { }
    private CraftMetaPotion(CraftMetaPotion source) { super(source); }
    @Override public CraftMetaPotion clone() { return new CraftMetaPotion(this); }
}
