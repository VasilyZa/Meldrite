package org.bukkit.craftbukkit.inventory;

@org.bukkit.configuration.serialization.DelegateDeserialization(CraftMetaItem.SerializableMeta.class)
public final class CraftMetaFirework extends dev.meldrite.paper.facade.FireworkItemMeta {
    public CraftMetaFirework() { }
    private CraftMetaFirework(CraftMetaFirework source) { super(source); }
    @Override public CraftMetaFirework clone() { return new CraftMetaFirework(this); }
    public static int getNBT(org.bukkit.FireworkEffect.Type type) { return ((Number) effectTag(org.bukkit.FireworkEffect.builder().with(type).withColor(org.bukkit.Color.WHITE).build()).get("Type")).intValue(); }
}
