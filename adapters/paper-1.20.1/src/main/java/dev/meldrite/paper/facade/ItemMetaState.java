package dev.meldrite.paper.facade;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.inventory.meta.tags.CustomItemTagContainer;
import org.bukkit.inventory.meta.tags.ItemTagAdapterContext;
import org.bukkit.inventory.meta.tags.ItemTagType;
import org.bukkit.persistence.PersistentDataContainer;

/** Common item metadata is stored in its native tag shape so unknown data is retained. */
public class ItemMetaState implements Damageable, Repairable {
    private static final GsonComponentSerializer COMPONENTS = GsonComponentSerializer.gson();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();
    private Map<String, Object> tag = new LinkedHashMap<>();
    private int version;

    public ItemMetaState() { }
    protected ItemMetaState(ItemMetaState source) { tag = source.nativeMetadata(); version = source.version; }
    public Map<String, Object> nativeMetadata() { Map<String, Object> result = ItemNbtData.copy(tag); if (result.get("PublicBukkitValues") instanceof Map<?, ?> values && values.isEmpty()) result.remove("PublicBukkitValues"); return result; }
    public void nativeMetadata(Map<String, Object> metadata) {
        tag = ItemNbtData.copy(metadata);
        if (tag.get("Damage") instanceof Number damage && damage.intValue() == 0) tag.remove("Damage");
    }
    protected final Map<String, Object> mutableTag() { return tag; }
    protected final Map<String, Object> mutableCompound(String name) {
        Object current = tag.get(name);
        if (current instanceof Map<?, ?>) return ItemNbtData.compound(current);
        Map<String, Object> result = new LinkedHashMap<>(); tag.put(name, result); return result;
    }
    private Map<String, Object> display() { return ItemNbtData.compound(tag.get("display")); }
    private void displayValue(String key, Object value) {
        if (value == null) {
            Map<String, Object> display = display();
            if (!display.isEmpty()) { display.remove(key); if (display.isEmpty()) tag.remove("display"); }
        } else mutableCompound("display").put(key, value);
    }
    private Component component(Object value) {
        if (!(value instanceof String text)) return null;
        try { return COMPONENTS.deserialize(text); }
        catch (com.google.gson.JsonParseException failure) { return LEGACY.deserialize(text); }
    }
    @Override public boolean hasDisplayName() { return display().containsKey("Name"); }
    @Override public Component displayName() { return component(display().get("Name")); }
    @Override public void displayName(Component value) { displayValue("Name", value == null ? null : COMPONENTS.serialize(value)); }
    @Override public String getDisplayName() { Component value = displayName(); return value == null ? "" : LEGACY.serialize(value); }
    @Override public void setDisplayName(String value) { displayName(value == null ? null : LEGACY.deserialize(value)); }
    @Override public BaseComponent[] getDisplayNameComponent() { Object value = display().get("Name"); return value == null ? null : ComponentSerializer.parse((String) value); }
    @Override public void setDisplayNameComponent(BaseComponent[] value) { displayValue("Name", value == null ? null : ComponentSerializer.toString(value)); }
    @Override public boolean hasLocalizedName() { return display().containsKey("LocName"); }
    @Override public String getLocalizedName() { Component value = component(display().get("LocName")); return value == null ? "" : LEGACY.serialize(value); }
    @Override public void setLocalizedName(String value) { displayValue("LocName", value == null ? null : COMPONENTS.serialize(LEGACY.deserialize(value))); }
    @Override public boolean hasLore() { return display().get("Lore") instanceof List<?> list && !list.isEmpty(); }
    @Override public List<Component> lore() { return display().get("Lore") instanceof List<?> list ? list.stream().map(this::component).toList() : null; }
    @Override public void lore(List<? extends Component> value) { displayValue("Lore", value == null ? null : new ArrayList<>(value.stream().map(COMPONENTS::serialize).toList())); }
    @Override public List<String> getLore() { List<Component> value = lore(); return value == null ? null : new ArrayList<>(value.stream().map(LEGACY::serialize).toList()); }
    @Override public void setLore(List<String> value) { lore(value == null ? null : value.stream().map(text -> LEGACY.deserialize(text == null ? "" : text)).toList()); }
    @Override public List<BaseComponent[]> getLoreComponents() { return display().get("Lore") instanceof List<?> list ? list.stream().map(value -> ComponentSerializer.parse((String) value)).toList() : null; }
    @Override public void setLoreComponents(List<BaseComponent[]> value) { displayValue("Lore", value == null ? null : new ArrayList<>(value.stream().map(ComponentSerializer::toString).toList())); }
    @Override public boolean hasCustomModelData() { return tag.get("CustomModelData") instanceof Number; }
    @Override public int getCustomModelData() { if (!hasCustomModelData()) throw new IllegalStateException("CUSTOM_MODEL_DATA_ABSENT"); return ((Number) tag.get("CustomModelData")).intValue(); }
    @Override public void setCustomModelData(Integer value) { if (value == null) tag.remove("CustomModelData"); else tag.put("CustomModelData", value); }

    private List<Map<String, Object>> enchantTags() {
        List<Map<String, Object>> result = new ArrayList<>();
        if (tag.get("Enchantments") instanceof List<?> list) for (Object value : list) if (value instanceof Map<?, ?>) result.add(ItemNbtData.compound(value));
        return result;
    }
    @Override public boolean hasEnchants() { return !enchantTags().isEmpty(); }
    @Override public boolean hasEnchant(Enchantment enchantment) { return enchantTags().stream().anyMatch(value -> enchantment.getKey().toString().equals(value.get("id"))); }
    @Override public int getEnchantLevel(Enchantment enchantment) { return enchantTags().stream().filter(value -> enchantment.getKey().toString().equals(value.get("id"))).map(value -> value.get("lvl")).filter(Number.class::isInstance).map(Number.class::cast).mapToInt(Number::intValue).findFirst().orElse(0); }
    @Override public Map<Enchantment, Integer> getEnchants() {
        Map<Enchantment, Integer> result = new LinkedHashMap<>();
        for (Map<String, Object> value : enchantTags()) {
            NamespacedKey key = NamespacedKey.fromString(String.valueOf(value.get("id")));
            Enchantment enchantment = key == null ? null : Enchantment.getByKey(key);
            if (enchantment != null && value.get("lvl") instanceof Number level) result.put(enchantment, level.intValue());
        }
        return Collections.unmodifiableMap(result);
    }
    @Override public boolean addEnchant(Enchantment enchantment, int level, boolean ignoreLevelRestriction) {
        Objects.requireNonNull(enchantment);
        if (!ignoreLevelRestriction && (level < enchantment.getStartLevel() || level > enchantment.getMaxLevel())) return false;
        if (hasEnchant(enchantment) && getEnchantLevel(enchantment) == level) return false;
        List<Map<String, Object>> values = enchantTags();
        values.removeIf(value -> enchantment.getKey().toString().equals(value.get("id")));
        values.add(new LinkedHashMap<>(Map.of("id", enchantment.getKey().toString(), "lvl", (short) level)));
        tag.put("Enchantments", values); return true;
    }
    @Override public boolean removeEnchant(Enchantment enchantment) { List<Map<String, Object>> values = enchantTags(); boolean removed = values.removeIf(value -> enchantment.getKey().toString().equals(value.get("id"))); if (values.isEmpty()) tag.remove("Enchantments"); else tag.put("Enchantments", values); return removed; }
    @Override public boolean hasConflictingEnchant(Enchantment enchantment) { return getEnchants().keySet().stream().anyMatch(enchantment::conflictsWith); }
    private int hideFlags() { return tag.get("HideFlags") instanceof Number value ? value.intValue() : 0; }
    @Override public void addItemFlags(ItemFlag... flags) { int value = hideFlags(); for (ItemFlag flag : flags) value |= 1 << flag.ordinal(); tag.put("HideFlags", value); }
    @Override public void removeItemFlags(ItemFlag... flags) { int value = hideFlags(); for (ItemFlag flag : flags) value &= ~(1 << flag.ordinal()); if (value == 0) tag.remove("HideFlags"); else tag.put("HideFlags", value); }
    @Override public Set<ItemFlag> getItemFlags() { Set<ItemFlag> result = EnumSet.noneOf(ItemFlag.class); for (ItemFlag flag : ItemFlag.values()) if (hasItemFlag(flag)) result.add(flag); return Set.copyOf(result); }
    @Override public boolean hasItemFlag(ItemFlag flag) { return (hideFlags() & 1 << flag.ordinal()) != 0; }
    @Override public boolean isUnbreakable() { return tag.get("Unbreakable") instanceof Number value && value.byteValue() != 0; }
    @Override public void setUnbreakable(boolean value) { if (value) tag.put("Unbreakable", (byte) 1); else tag.remove("Unbreakable"); }
    @Override public boolean hasDamage() { return getDamage() != 0; }
    @Override public int getDamage() { return tag.get("Damage") instanceof Number value ? value.intValue() : 0; }
    @Override public void setDamage(int value) { if (value < 0) throw new IllegalArgumentException("Damage must be non-negative"); if (value == 0) tag.remove("Damage"); else tag.put("Damage", value); }
    @Override public boolean hasRepairCost() { return getRepairCost() != 0; }
    @Override public int getRepairCost() { return tag.get("RepairCost") instanceof Number value ? value.intValue() : 0; }
    @Override public void setRepairCost(int value) { if (value < 0) throw new IllegalArgumentException("Repair cost must be non-negative"); if (value == 0) tag.remove("RepairCost"); else tag.put("RepairCost", value); }

    @Override public boolean hasAttributeModifiers() { return tag.get("AttributeModifiers") instanceof List<?> values && !values.isEmpty(); }
    @Override public Multimap<Attribute, AttributeModifier> getAttributeModifiers() {
        if (!hasAttributeModifiers()) return null;
        Multimap<Attribute, AttributeModifier> result = ArrayListMultimap.create();
        for (Object entry : (List<?>) tag.get("AttributeModifiers")) {
            Map<String, Object> value = ItemNbtData.compound(entry);
            Attribute attribute = Arrays.stream(Attribute.values()).filter(candidate -> candidate.getKey().toString().equals(value.get("AttributeName"))).findFirst().orElse(null);
            UUID uuid = ItemNbtData.uuid(value.get("UUID"));
            if (attribute == null || uuid == null || !(value.get("Amount") instanceof Number amount) || !(value.get("Operation") instanceof Number operation)) continue;
            int ordinal = operation.intValue();
            if (ordinal < 0 || ordinal >= AttributeModifier.Operation.values().length) continue;
            EquipmentSlot slot = slot(value.get("Slot"));
            result.put(attribute, new AttributeModifier(uuid, String.valueOf(value.get("Name")), amount.doubleValue(), AttributeModifier.Operation.values()[ordinal], slot));
        }
        return com.google.common.collect.ImmutableMultimap.copyOf(result);
    }
    private static EquipmentSlot slot(Object value) {
        if (!(value instanceof String text)) return null;
        return switch (text) { case "mainhand" -> EquipmentSlot.HAND; case "offhand" -> EquipmentSlot.OFF_HAND; case "feet" -> EquipmentSlot.FEET; case "legs" -> EquipmentSlot.LEGS; case "chest" -> EquipmentSlot.CHEST; case "head" -> EquipmentSlot.HEAD; default -> null; };
    }
    private static String slot(EquipmentSlot value) { return switch (value) { case HAND -> "mainhand"; case OFF_HAND -> "offhand"; case FEET -> "feet"; case LEGS -> "legs"; case CHEST -> "chest"; case HEAD -> "head"; }; }
    @Override public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot) { Multimap<Attribute, AttributeModifier> all = getAttributeModifiers(); if (all == null) return null; Multimap<Attribute, AttributeModifier> result = ArrayListMultimap.create(); all.entries().stream().filter(entry -> entry.getValue().getSlot() == null || entry.getValue().getSlot() == slot).forEach(entry -> result.put(entry.getKey(), entry.getValue())); return com.google.common.collect.ImmutableMultimap.copyOf(result); }
    @Override public Collection<AttributeModifier> getAttributeModifiers(Attribute attribute) { Multimap<Attribute, AttributeModifier> all = getAttributeModifiers(); return all == null ? null : List.copyOf(all.get(attribute)); }
    @Override public boolean addAttributeModifier(Attribute attribute, AttributeModifier modifier) { Multimap<Attribute, AttributeModifier> all = getAttributeModifiers(); Multimap<Attribute, AttributeModifier> values = all == null ? ArrayListMultimap.create() : ArrayListMultimap.create(all); if (values.values().stream().anyMatch(value -> value.getUniqueId().equals(modifier.getUniqueId()))) throw new IllegalArgumentException("DUPLICATE_ATTRIBUTE_UUID"); values.put(attribute, modifier); setAttributeModifiers(values); return true; }
    @Override public void setAttributeModifiers(Multimap<Attribute, AttributeModifier> modifiers) {
        if (modifiers == null || modifiers.isEmpty()) { tag.remove("AttributeModifiers"); return; }
        List<Object> values = new ArrayList<>();
        for (var entry : modifiers.entries()) {
            AttributeModifier modifier = entry.getValue(); Map<String, Object> value = new LinkedHashMap<>();
            value.put("AttributeName", entry.getKey().getKey().toString()); value.put("Name", modifier.getName()); value.put("Amount", modifier.getAmount()); value.put("Operation", modifier.getOperation().ordinal()); value.put("UUID", modifier.getUniqueId());
            if (modifier.getSlot() != null) value.put("Slot", slot(modifier.getSlot())); values.add(value);
        }
        tag.put("AttributeModifiers", values);
    }
    @Override public boolean removeAttributeModifier(Attribute attribute) { Multimap<Attribute, AttributeModifier> values = getAttributeModifiers(); if (values == null) return false; values = ArrayListMultimap.create(values); boolean removed = !values.removeAll(attribute).isEmpty(); setAttributeModifiers(values); return removed; }
    @Override public boolean removeAttributeModifier(EquipmentSlot slot) { Multimap<Attribute, AttributeModifier> values = getAttributeModifiers(); if (values == null) return false; values = ArrayListMultimap.create(values); boolean removed = values.entries().removeIf(entry -> Objects.equals(entry.getValue().getSlot(), slot)); setAttributeModifiers(values); return removed; }
    @Override public boolean removeAttributeModifier(Attribute attribute, AttributeModifier modifier) { Multimap<Attribute, AttributeModifier> values = getAttributeModifiers(); if (values == null) return false; values = ArrayListMultimap.create(values); boolean removed = values.remove(attribute, modifier); setAttributeModifiers(values); return removed; }

    private Set<com.destroystokyo.paper.Namespaced> keys(String name) { Set<com.destroystokyo.paper.Namespaced> result = new LinkedHashSet<>(); if (tag.get(name) instanceof List<?> list) for (Object value : list) { NamespacedKey key = NamespacedKey.fromString(String.valueOf(value)); if (key != null) result.add(key); } return Set.copyOf(result); }
    private void keys(String name, Collection<com.destroystokyo.paper.Namespaced> values) { if (values == null || values.isEmpty()) tag.remove(name); else tag.put(name, new ArrayList<>(values.stream().map(value -> value.getNamespace() + ":" + value.getKey()).toList())); }
    private Set<Material> materials(String name) { Set<Material> result = EnumSet.noneOf(Material.class); for (var value : keys(name)) { Material material = Material.matchMaterial(value.getNamespace() + ":" + value.getKey()); if (material != null) result.add(material); } return Set.copyOf(result); }
    @Override public Set<Material> getCanDestroy() { return materials("CanDestroy"); }
    @Override public void setCanDestroy(Set<Material> values) { keys("CanDestroy", values == null ? null : values.stream().map(Material::getKey).map(com.destroystokyo.paper.Namespaced.class::cast).toList()); }
    @Override public Set<Material> getCanPlaceOn() { return materials("CanPlaceOn"); }
    @Override public void setCanPlaceOn(Set<Material> values) { keys("CanPlaceOn", values == null ? null : values.stream().map(Material::getKey).map(com.destroystokyo.paper.Namespaced.class::cast).toList()); }
    @Override public Set<com.destroystokyo.paper.Namespaced> getDestroyableKeys() { return keys("CanDestroy"); }
    @Override public void setDestroyableKeys(Collection<com.destroystokyo.paper.Namespaced> values) { keys("CanDestroy", values); }
    @Override public Set<com.destroystokyo.paper.Namespaced> getPlaceableKeys() { return keys("CanPlaceOn"); }
    @Override public void setPlaceableKeys(Collection<com.destroystokyo.paper.Namespaced> values) { keys("CanPlaceOn", values); }
    @Override public boolean hasPlaceableKeys() { return !keys("CanPlaceOn").isEmpty(); }
    @Override public boolean hasDestroyableKeys() { return !keys("CanDestroy").isEmpty(); }
    @Override public PersistentDataContainer getPersistentDataContainer() { return new ItemPersistentDataContainer(() -> mutableCompound("PublicBukkitValues")); }
    @Override public CustomItemTagContainer getCustomTagContainer() { return new CustomTags(() -> mutableCompound("PublicBukkitValues")); }
    @Override public void setVersion(int value) { version = value; }
    @Override public ItemMetaState clone() { return new ItemMetaState(this); }
    @Override public String getAsString() { return ItemNbtData.snbt(nativeMetadata()); }
    @Override public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>(); result.put("meta-type", "UNSPECIFIC");
        if (hasDisplayName()) result.put("display-name", display().get("Name"));
        if (hasLocalizedName()) result.put("localized-name", display().get("LocName"));
        if (hasLore()) result.put("lore", ItemNbtData.copyValue(display().get("Lore")));
        if (hasCustomModelData()) result.put("custom-model-data", getCustomModelData());
        if (isUnbreakable()) result.put("unbreakable", true);
        if (hasDamage()) result.put("damage", getDamage());
        if (hasRepairCost()) result.put("repair-cost", getRepairCost());
        if (hasEnchants()) { Map<String, Integer> enchants = new LinkedHashMap<>(); getEnchants().forEach((enchant, level) -> enchants.put(enchant.getName(), level)); result.put("enchants", enchants); }
        if (hideFlags() != 0) result.put("ItemFlags", getItemFlags().stream().map(Enum::name).toList());
        if (!nativeMetadata().isEmpty()) {
            try { result.put("internal", Base64.getEncoder().encodeToString(ItemNbtData.encode(nativeMetadata()))); }
            catch (java.io.IOException failure) { throw new java.io.UncheckedIOException("Cannot serialize item metadata", failure); }
        }
        return result;
    }
    @Override public boolean equals(Object value) { return value instanceof ItemMetaState other && getAsString().equals(other.getAsString()); }
    @Override public int hashCode() { return getAsString().hashCode(); }
    @Override public String toString() { return getClass().getSimpleName() + getAsString(); }

    private static final class CustomTags implements CustomItemTagContainer {
        private final java.util.function.Supplier<Map<String, Object>> values;
        private final ItemTagAdapterContext context = () -> { Map<String, Object> value = new LinkedHashMap<>(); return new CustomTags(() -> value); };
        private CustomTags(java.util.function.Supplier<Map<String, Object>> values) { this.values = values; }
        @Override public <T, Z> void setCustomTag(NamespacedKey key, ItemTagType<T, Z> type, Z value) { Object primitive = type.toPrimitive(value, context); if (primitive instanceof CustomTags container) primitive = container.values.get(); values.get().put(key.toString(), ItemNbtData.copyValue(primitive)); }
        @Override public <T, Z> boolean hasCustomTag(NamespacedKey key, ItemTagType<T, Z> type) { return primitive(key, type) != null; }
        @SuppressWarnings("unchecked") private <T, Z> T primitive(NamespacedKey key, ItemTagType<T, Z> type) { Object value = values.get().get(key.toString()); if (type.getPrimitiveType() == CustomItemTagContainer.class && value instanceof Map<?, ?> map) { Map<String, Object> copy = ItemNbtData.copy(ItemNbtData.compound(map)); return (T) new CustomTags(() -> copy); } return type.getPrimitiveType().isInstance(value) ? (T) ItemNbtData.copyValue(value) : null; }
        @Override public <T, Z> Z getCustomTag(NamespacedKey key, ItemTagType<T, Z> type) { T primitive = primitive(key, type); return primitive == null ? null : type.fromPrimitive(primitive, context); }
        @Override public void removeCustomTag(NamespacedKey key) { values.get().remove(key.toString()); }
        @Override public boolean isEmpty() { return values.get().isEmpty(); }
        @Override public ItemTagAdapterContext getAdapterContext() { return context; }
    }
}
