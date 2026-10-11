package dev.meldrite.paper.facade;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

public class EnchantedBookItemMeta extends org.bukkit.craftbukkit.inventory.CraftMetaItem implements EnchantmentStorageMeta {
    protected EnchantedBookItemMeta() {}
    protected EnchantedBookItemMeta(EnchantedBookItemMeta source) { super(source); }
    private List<Map<String, Object>> entries() {
        List<Map<String, Object>> result = new ArrayList<>();
        if (mutableTag().get("StoredEnchantments") instanceof List<?> entries)
            for (Object value : entries) if (value instanceof Map<?, ?>) result.add(ItemNbtData.compound(value));
        return result;
    }
    @Override public boolean hasStoredEnchants() { return !entries().isEmpty(); }
    @Override public boolean hasStoredEnchant(Enchantment enchantment) { return entries().stream().anyMatch(value -> Objects.requireNonNull(enchantment).getKey().toString().equals(value.get("id"))); }
    @Override public int getStoredEnchantLevel(Enchantment enchantment) {
        Objects.requireNonNull(enchantment);
        return entries().stream().filter(value -> enchantment.getKey().toString().equals(value.get("id")))
                .map(value -> value.get("lvl")).filter(Number.class::isInstance).map(Number.class::cast).mapToInt(Number::intValue).findFirst().orElse(0);
    }
    @Override public Map<Enchantment, Integer> getStoredEnchants() {
        Map<Enchantment, Integer> result = new LinkedHashMap<>();
        for (Map<String, Object> value : entries()) {
            NamespacedKey key = value.get("id") instanceof String id ? NamespacedKey.fromString(id) : null;
            Enchantment enchantment = key == null ? null : Enchantment.getByKey(key);
            if (enchantment != null && value.get("lvl") instanceof Number level) result.put(enchantment, level.intValue());
        }
        return Map.copyOf(result);
    }
    @Override public boolean addStoredEnchant(Enchantment enchantment, int level, boolean ignoreLevelRestriction) {
        Objects.requireNonNull(enchantment);
        if (!ignoreLevelRestriction && (level < enchantment.getStartLevel() || level > enchantment.getMaxLevel())) return false;
        if (hasStoredEnchant(enchantment) && getStoredEnchantLevel(enchantment) == level) return false;
        List<Map<String, Object>> entries = entries();
        entries.removeIf(value -> enchantment.getKey().toString().equals(value.get("id")));
        entries.add(Map.of("id", enchantment.getKey().toString(), "lvl", (short) level));
        mutableTag().put("StoredEnchantments", entries);
        return true;
    }
    @Override public boolean removeStoredEnchant(Enchantment enchantment) {
        Objects.requireNonNull(enchantment);
        List<Map<String, Object>> entries = entries();
        boolean removed = entries.removeIf(value -> enchantment.getKey().toString().equals(value.get("id")));
        if (entries.isEmpty()) mutableTag().remove("StoredEnchantments"); else mutableTag().put("StoredEnchantments", entries);
        return removed;
    }
    @Override public boolean hasConflictingStoredEnchant(Enchantment enchantment) { return getStoredEnchants().keySet().stream().anyMatch(Objects.requireNonNull(enchantment)::conflictsWith); }
    @Override public EnchantedBookItemMeta clone() { return new EnchantedBookItemMeta(this); }
    @Override public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize(); result.put("meta-type", "ENCHANTED");
        if (hasStoredEnchants()) { Map<String, Integer> enchants = new LinkedHashMap<>(); getStoredEnchants().forEach((enchant, level) -> enchants.put(enchant.getName(), level)); result.put("stored-enchants", enchants); }
        return result;
    }
}
