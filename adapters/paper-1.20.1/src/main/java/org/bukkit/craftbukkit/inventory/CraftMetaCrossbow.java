package org.bukkit.craftbukkit.inventory;

import dev.meldrite.paper.facade.ItemFactoryAdapter;
import dev.meldrite.paper.facade.ItemNbtData;
import java.util.*;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;

@org.bukkit.configuration.serialization.DelegateDeserialization(CraftMetaItem.SerializableMeta.class)
public final class CraftMetaCrossbow extends CraftMetaItem implements CrossbowMeta {
    public CraftMetaCrossbow() { }
    private CraftMetaCrossbow(CraftMetaCrossbow source) { super(source); }
    @Override public boolean hasChargedProjectiles() { return mutableTag().get("ChargedProjectiles") instanceof List<?> list && !list.isEmpty(); }
    @Override public List<ItemStack> getChargedProjectiles() {
        if (!(mutableTag().get("ChargedProjectiles") instanceof List<?> list)) return List.of();
        return list.stream().map(entry -> ItemFactoryAdapter.itemFromData(ItemNbtData.compound(entry))).toList();
    }
    private static Map<String, Object> projectile(ItemStack item) {
        Objects.requireNonNull(item, "projectile");
        if (item.getAmount() <= 0 || !ItemFactoryAdapter.isCrossbowProjectile(item)) throw new IllegalArgumentException("Projectile must be an arrow or firework rocket");
        return ItemFactoryAdapter.itemData(item);
    }
    @Override public void setChargedProjectiles(List<ItemStack> items) {
        List<Object> entries = new ArrayList<>(); if (items != null) for (ItemStack item : items) entries.add(projectile(item));
        value("ChargedProjectiles", entries.isEmpty() ? null : entries); value("Charged", (byte) (entries.isEmpty() ? 0 : 1));
    }
    @Override public void addChargedProjectile(ItemStack item) {
        Map<String, Object> entry = projectile(item);
        List<Object> entries = mutableTag().get("ChargedProjectiles") instanceof List<?> list ? new ArrayList<>(list) : new ArrayList<>(); entries.add(entry);
        value("ChargedProjectiles", entries); value("Charged", (byte) 1);
    }
    @Override public CraftMetaCrossbow clone() { return new CraftMetaCrossbow(this); }
    @Override public Map<String, Object> serialize() { return serialized("CROSSBOW"); }
}
