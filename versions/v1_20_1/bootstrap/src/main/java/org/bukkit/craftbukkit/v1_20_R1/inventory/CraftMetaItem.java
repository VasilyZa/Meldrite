package org.bukkit.craftbukkit.v1_20_R1.inventory;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import org.bukkit.enchantments.Enchantment;

/** Versioned metadata ABI used by plugins that reflect CraftBukkit internals. */
public class CraftMetaItem extends org.bukkit.craftbukkit.inventory.CraftMetaItem {
    public CraftMetaItem() { super(); }
    protected CraftMetaItem(org.bukkit.craftbukkit.inventory.CraftMetaItem source) { super(source); }
    @Override public CraftMetaItem clone() { return new CraftMetaItem(this); }
    public static class ItemMetaKey {
        public final String BUKKIT;
        public final String NBT;
        public ItemMetaKey(String both) { this(both, both); }
        public ItemMetaKey(String nbt, String bukkit) { this.NBT = nbt; this.BUKKIT = bukkit; }
        @Retention(RetentionPolicy.SOURCE) @Target(ElementType.FIELD)
        public @interface Specific {
            To value();
            enum To { BUKKIT, NBT }
        }
    }
    public static class EnchantmentMap extends TreeMap<Enchantment, Integer> {
        public EnchantmentMap() { super(Comparator.comparing(value -> value.getKey().toString())); }
        public EnchantmentMap(Map<Enchantment, Integer> values) { this(); putAll(values); }
        @Override public EnchantmentMap clone() { return (EnchantmentMap) super.clone(); }
    }
}
