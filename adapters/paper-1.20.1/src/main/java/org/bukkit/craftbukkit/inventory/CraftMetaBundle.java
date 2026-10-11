package org.bukkit.craftbukkit.inventory;

import dev.meldrite.paper.facade.ItemFactoryAdapter;
import dev.meldrite.paper.facade.ItemNbtData;
import java.util.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;

@org.bukkit.configuration.serialization.DelegateDeserialization(CraftMetaItem.SerializableMeta.class)
public final class CraftMetaBundle extends CraftMetaItem implements BundleMeta {
    public CraftMetaBundle() { }
    private CraftMetaBundle(CraftMetaBundle source) { super(source); }
    @Override public boolean hasItems() { return mutableTag().get("Items") instanceof List<?> list && !list.isEmpty(); }
    @Override public List<ItemStack> getItems() { return mutableTag().get("Items") instanceof List<?> list ? list.stream().map(item -> ItemFactoryAdapter.itemFromData(ItemNbtData.compound(item))).toList() : List.of(); }
    private static Map<String, Object> item(ItemStack item) { Objects.requireNonNull(item, "item"); if (item.getType().isAir() || item.getAmount() <= 0) throw new IllegalArgumentException("Bundle item must not be empty"); return ItemFactoryAdapter.itemData(item); }
    @Override public void setItems(List<ItemStack> items) { List<Object> entries = new ArrayList<>(); if (items != null) for (ItemStack item : items) entries.add(item(item)); value("Items", entries.isEmpty() ? null : entries); }
    @Override public void addItem(ItemStack item) { Map<String, Object> entry = item(item); List<Object> entries = mutableTag().get("Items") instanceof List<?> list ? new ArrayList<>(list) : new ArrayList<>(); entries.add(entry); value("Items", entries); }
    @Override public CraftMetaBundle clone() { return new CraftMetaBundle(this); }
    @Override public Map<String, Object> serialize() { return serialized("BUNDLE"); }
}
