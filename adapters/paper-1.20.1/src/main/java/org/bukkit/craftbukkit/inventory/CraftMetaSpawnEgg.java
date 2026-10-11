package org.bukkit.craftbukkit.inventory;

@org.bukkit.configuration.serialization.DelegateDeserialization(CraftMetaItem.SerializableMeta.class)
public final class CraftMetaSpawnEgg extends dev.meldrite.paper.facade.SpawnEggItemMeta {
    public CraftMetaSpawnEgg() { }
    public CraftMetaSpawnEgg(org.bukkit.entity.EntityType type) { super(type); }
    private CraftMetaSpawnEgg(CraftMetaSpawnEgg source) { super(source); }
    @Override public CraftMetaSpawnEgg clone() { return new CraftMetaSpawnEgg(this); }
}
