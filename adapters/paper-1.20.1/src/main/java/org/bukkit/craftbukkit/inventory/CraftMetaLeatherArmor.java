package org.bukkit.craftbukkit.inventory;

@org.bukkit.configuration.serialization.DelegateDeserialization(CraftMetaItem.SerializableMeta.class)
public class CraftMetaLeatherArmor extends dev.meldrite.paper.facade.LeatherItemMeta {
    public CraftMetaLeatherArmor() { }
    protected CraftMetaLeatherArmor(CraftMetaLeatherArmor source) { super(source); }
    @Override public CraftMetaLeatherArmor clone() { return new CraftMetaLeatherArmor(this); }
}
