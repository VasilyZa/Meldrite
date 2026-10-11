package org.bukkit.craftbukkit.inventory;

import java.util.*;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.KnowledgeBookMeta;

@org.bukkit.configuration.serialization.DelegateDeserialization(CraftMetaItem.SerializableMeta.class)
public final class CraftMetaKnowledgeBook extends CraftMetaItem implements KnowledgeBookMeta {
    public CraftMetaKnowledgeBook() { }
    private CraftMetaKnowledgeBook(CraftMetaKnowledgeBook source) { super(source); }
    @Override public boolean hasRecipes() { return mutableTag().get("Recipes") instanceof List<?> list && !list.isEmpty(); }
    @Override public List<NamespacedKey> getRecipes() { return mutableTag().get("Recipes") instanceof List<?> list ? list.stream().map(value -> NamespacedKey.fromString(String.valueOf(value))).filter(Objects::nonNull).toList() : List.of(); }
    @Override public void setRecipes(List<NamespacedKey> recipes) { value("Recipes", new ArrayList<>(Objects.requireNonNull(recipes).stream().map(key -> Objects.requireNonNull(key).toString()).toList())); }
    @Override public void addRecipe(NamespacedKey... recipes) { List<NamespacedKey> values = new ArrayList<>(getRecipes()); values.addAll(Arrays.asList(recipes)); setRecipes(values); }
    @Override public CraftMetaKnowledgeBook clone() { return new CraftMetaKnowledgeBook(this); }
    @Override public Map<String, Object> serialize() { Map<String, Object> result = serialized("KNOWLEDGE_BOOK"); if (hasRecipes()) result.put("recipes", getRecipes().stream().map(NamespacedKey::toString).toList()); return result; }
}
