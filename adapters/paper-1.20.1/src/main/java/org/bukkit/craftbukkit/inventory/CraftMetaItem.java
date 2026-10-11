package org.bukkit.craftbukkit.inventory;

import dev.meldrite.paper.facade.ItemMetaState;
import dev.meldrite.paper.facade.ItemNbtData;
import dev.meldrite.paper.facade.ItemFactoryAdapter;
import java.util.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.meta.*;

/** Common Craft metadata retains the complete native item tag. */
@org.bukkit.configuration.serialization.DelegateDeserialization(CraftMetaItem.SerializableMeta.class)
public class CraftMetaItem extends ItemMetaState {
    static { ConfigurationSerialization.registerClass(SerializableMeta.class, "ItemMeta"); }
    public CraftMetaItem() { }
    protected CraftMetaItem(ItemMetaState source) { super(source); }
    protected final int number(String key, int fallback) { return mutableTag().get(key) instanceof Number value ? value.intValue() : fallback; }
    protected final void value(String key, Object value) { if (value == null) mutableTag().remove(key); else mutableTag().put(key, value); }
    protected final Map<String, Object> serialized(String type) { Map<String, Object> result = super.serialize(); result.put("meta-type", type); return result; }
    @Override public CraftMetaItem clone() { return new CraftMetaItem(this); }

    @SerializableAs("ItemMeta")
    public static final class SerializableMeta implements ConfigurationSerializable {
        private SerializableMeta() { }
        public static ItemMeta deserialize(Map<String, Object> map) {
            Objects.requireNonNull(map, "map");
            String type = getString(map, "meta-type", false);
            Material material = switch (type) {
                case "UNSPECIFIC" -> Material.STONE; case "BOOK" -> Material.WRITABLE_BOOK; case "BOOK_SIGNED" -> Material.WRITTEN_BOOK;
                case "POTION" -> Material.POTION; case "SKULL" -> Material.PLAYER_HEAD; case "ENCHANTED" -> Material.ENCHANTED_BOOK;
                case "LEATHER_ARMOR" -> Material.LEATHER_HORSE_ARMOR; case "ARMOR" -> Material.DIAMOND_CHESTPLATE; case "COLORABLE_ARMOR" -> Material.LEATHER_CHESTPLATE;
                case "SPAWN_EGG" -> Material.PIG_SPAWN_EGG; case "FIREWORK" -> Material.FIREWORK_ROCKET; case "FIREWORK_EFFECT" -> Material.FIREWORK_STAR;
                case "MAP" -> Material.FILLED_MAP; case "BANNER" -> Material.WHITE_BANNER; case "CROSSBOW" -> Material.CROSSBOW; case "BUNDLE" -> Material.BUNDLE;
                case "COMPASS" -> Material.COMPASS; case "ENTITY_TAG" -> Material.COD_BUCKET; case "AXOLOTL_BUCKET" -> Material.AXOLOTL_BUCKET;
                case "TROPICAL_FISH_BUCKET" -> Material.TROPICAL_FISH_BUCKET; case "KNOWLEDGE_BOOK" -> Material.KNOWLEDGE_BOOK;
                case "MUSIC_INSTRUMENT" -> Material.GOAT_HORN; case "SUSPICIOUS_STEW" -> Material.SUSPICIOUS_STEW; case "ARMOR_STAND" -> Material.ARMOR_STAND;
                case "TILE_ENTITY" -> Material.valueOf(getString(map, "blockMaterial", false));
                default -> throw new IllegalArgumentException("Invalid meta-type " + type);
            };
            ItemMeta result = new ItemFactoryAdapter().getItemMeta(material);
            if (!(result instanceof ItemMetaState state)) throw new IllegalArgumentException("Invalid metadata material " + material);
            Map<String, Object> tag = new LinkedHashMap<>();
            if (map.get("internal") instanceof String internal) {
                try { tag = ItemNbtData.decode(Base64.getDecoder().decode(internal)); }
                catch (java.io.IOException failure) { throw new IllegalArgumentException("Invalid compressed item metadata", failure); }
            }
            copyDisplay(map, tag, "display-name", "Name"); copyDisplay(map, tag, "localized-name", "LocName"); copyDisplay(map, tag, "lore", "Lore");
            for (String[] pair : List.of(new String[]{"custom-model-data", "CustomModelData"}, new String[]{"damage", "Damage"}, new String[]{"repair-cost", "RepairCost"})) if (map.get(pair[0]) instanceof Number value) tag.put(pair[1], value.intValue());
            if (map.get("unbreakable") instanceof Boolean value) { if (value) tag.put("Unbreakable", (byte) 1); else tag.remove("Unbreakable"); }
            state.nativeMetadata(tag);
            if (map.get("ItemFlags") instanceof Iterable<?> flags) {
                result.removeItemFlags(org.bukkit.inventory.ItemFlag.values());
                for (Object flag : flags) result.addItemFlags(org.bukkit.inventory.ItemFlag.valueOf(flag.toString()));
            }
            if (map.get("enchants") instanceof Map<?, ?> enchants) applyEnchants(result, enchants, false);
            if (result instanceof EnchantmentStorageMeta && map.get("stored-enchants") instanceof Map<?, ?> enchants) applyEnchants(result, enchants, true);
            if (result instanceof BookMeta book) {
                Map<String, Object> bookTag = state.nativeMetadata();
                for (String key : List.of("title", "author", "pages")) if (map.containsKey(key)) bookTag.put(key, ItemNbtData.copyValue(map.get(key)));
                if (map.get("generation") != null) bookTag.put("generation", BookMeta.Generation.valueOf(map.get("generation").toString()).ordinal());
                state.nativeMetadata(bookTag);
            }
            if (result instanceof LeatherArmorMeta leather && map.get("color") instanceof org.bukkit.Color color) leather.setColor(color);
            if (result instanceof ArmorMeta armor && map.get("trim") instanceof Map<?, ?> trim) { Map<String, Object> data = state.nativeMetadata(); data.put("Trim", ItemNbtData.copy(ItemNbtData.compound(trim))); state.nativeMetadata(data); }
            if (result instanceof MapMeta meta) {
                if (map.get("map-id") instanceof Number id) meta.setMapId(id.intValue());
                if (map.get("scaling") instanceof Boolean scaling) meta.setScaling(scaling);
                if (map.get("loc-name") instanceof String name) meta.setLocationName(name);
                if (map.get("color") instanceof org.bukkit.Color color) meta.setColor(color);
            }
            if (result instanceof BannerMeta banner) {
                if (map.get("base-color") != null) banner.setBaseColor(org.bukkit.DyeColor.valueOf(map.get("base-color").toString()));
                if (map.get("patterns") instanceof List<?> patterns) {
                    List<org.bukkit.block.banner.Pattern> parsed = new ArrayList<>();
                    for (Object entry : patterns) {
                        if (entry instanceof org.bukkit.block.banner.Pattern pattern) parsed.add(pattern);
                        else if (entry instanceof Map<?, ?> value) {
                            org.bukkit.DyeColor color = org.bukkit.DyeColor.valueOf(String.valueOf(value.get("color")));
                            org.bukkit.block.banner.PatternType pattern = org.bukkit.block.banner.PatternType.getByIdentifier(String.valueOf(value.get("pattern")));
                            if (pattern != null) parsed.add(new org.bukkit.block.banner.Pattern(color, pattern));
                        }
                    }
                    if (!banner.getPatterns().equals(parsed)) banner.setPatterns(parsed);
                }
            }
            if (result instanceof FireworkEffectMeta charge && map.get("firework-effect") instanceof org.bukkit.FireworkEffect effect && !Objects.equals(charge.getEffect(), effect)) charge.setEffect(effect);
            if (result instanceof FireworkMeta firework && map.get("effects") instanceof List<?> effects && !firework.getEffects().equals(effects)) { firework.clearEffects(); for (Object effect : effects) firework.addEffect((org.bukkit.FireworkEffect) effect); }
            if (result instanceof PotionMeta potion) {
                if (map.get("custom-color") instanceof org.bukkit.Color color) potion.setColor(color);
                if (map.get("potion-type") instanceof String potionType) { Map<String, Object> data = state.nativeMetadata(); data.put("Potion", potionType); state.nativeMetadata(data); }
                if (map.get("custom-effects") instanceof List<?> effects && !potion.getCustomEffects().equals(effects)) { potion.clearCustomEffects(); for (Object effect : effects) potion.addCustomEffect((org.bukkit.potion.PotionEffect) effect, true); }
            }
            if (result instanceof SuspiciousStewMeta stew && map.get("effects") instanceof List<?> effects && !stew.getCustomEffects().equals(effects)) { stew.clearCustomEffects(); for (Object effect : effects) stew.addCustomEffect((org.bukkit.potion.PotionEffect) effect, true); }
            if (result instanceof KnowledgeBookMeta book && map.get("recipes") instanceof List<?> recipes) { List<NamespacedKey> keys = recipes.stream().map(value -> Objects.requireNonNull(NamespacedKey.fromString(value.toString()))).toList(); if (!book.getRecipes().equals(keys)) book.setRecipes(keys); }
            if (result instanceof CraftMetaAxolotlBucket && map.get("axolotl-variant") instanceof Number variant) { Map<String, Object> data = state.nativeMetadata(); data.put("Variant", variant.intValue()); state.nativeMetadata(data); }
            if (result instanceof CraftMetaTropicalFishBucket && map.get("fish-variant") instanceof Number variant) { Map<String, Object> data = state.nativeMetadata(); data.put("BucketVariantTag", variant.intValue()); state.nativeMetadata(data); }
            if (result instanceof SkullMeta skull) {
                if (map.get("skull-owner") instanceof String owner) skull.setOwner(owner);
                else if (map.get("skull-owner") instanceof Map<?, ?> owner) {
                    Map<String, Object> data = ItemNbtData.compound(owner);
                    var current = skull.getPlayerProfile();
                    if (!(current instanceof dev.meldrite.paper.facade.PlayerProfileAdapter profile) || !profile.serialize().equals(data)) {
                        String name = data.get("name") instanceof String text ? text : null;
                        UUID id = data.get("uniqueId") instanceof String text ? UUID.fromString(text) : null;
                        var profile = new dev.meldrite.paper.facade.PlayerProfileAdapter(new com.mojang.authlib.GameProfile(id, name));
                        if (data.get("properties") instanceof Iterable<?> properties) for (Object property : properties) {
                            Map<String, Object> value = ItemNbtData.compound(property);
                            profile.setProperty(new com.destroystokyo.paper.profile.ProfileProperty((String) value.get("name"), (String) value.get("value"), (String) value.get("signature")));
                        }
                        skull.setPlayerProfile(profile);
                    }
                }
                if (map.get("note_block_sound") instanceof String sound) skull.setNoteBlockSound(NamespacedKey.fromString(sound));
            }
            if (result instanceof org.bukkit.inventory.meta.SpawnEggMeta egg && map.get("spawned-type") instanceof String spawnedTypeName) {
                try { egg.setSpawnedType(org.bukkit.entity.EntityType.valueOf(spawnedTypeName)); }
                catch (IllegalArgumentException failure) { throw new IllegalArgumentException("Unknown spawned entity type " + spawnedTypeName, failure); }
            }
            return result;
        }
        private static void copyDisplay(Map<String, Object> map, Map<String, Object> tag, String source, String target) {
            if (!map.containsKey(source)) return;
            Map<String, Object> display = ItemNbtData.copy(ItemNbtData.compound(tag.get("display")));
            Object value = map.get(source); if (value == null) display.remove(target); else display.put(target, ItemNbtData.copyValue(value));
            if (display.isEmpty()) tag.remove("display"); else tag.put("display", display);
        }
        private static void applyEnchants(ItemMeta meta, Map<?, ?> values, boolean stored) {
            for (var entry : values.entrySet()) {
                NamespacedKey key = NamespacedKey.fromString(entry.getKey().toString().toLowerCase(Locale.ROOT));
                var enchant = key == null ? null : org.bukkit.enchantments.Enchantment.getByKey(key);
                if (enchant == null) enchant = org.bukkit.enchantments.Enchantment.getByName(entry.getKey().toString());
                if (enchant != null && entry.getValue() instanceof Number level) {
                    if (stored) ((EnchantmentStorageMeta) meta).addStoredEnchant(enchant, level.intValue(), true); else meta.addEnchant(enchant, level.intValue(), true);
                }
            }
        }
        public static <T> T getObject(Class<T> type, Map<?, ?> map, Object field, boolean nullable) {
            Object value = map.get(field);
            if (value == null && nullable) return null;
            if (!type.isInstance(value)) throw new IllegalArgumentException("Expected " + type.getSimpleName() + " for " + field);
            return type.cast(value);
        }
        public static String getString(Map<?, ?> map, Object field, boolean nullable) { return getObject(String.class, map, field, nullable); }
        public static Boolean getBoolean(Map<?, ?> map, Object field) { return getObject(Boolean.class, map, field, true); }
        @Override public Map<String, Object> serialize() { throw new IllegalStateException("SerializableMeta is a deserialization delegate"); }
    }
}
