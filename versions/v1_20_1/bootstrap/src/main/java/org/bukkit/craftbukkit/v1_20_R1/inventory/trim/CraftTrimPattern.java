package org.bukkit.craftbukkit.v1_20_R1.inventory.trim;

import net.minecraft.item.trim.ArmorTrimPattern;
import org.bukkit.NamespacedKey;

public final class CraftTrimPattern implements org.bukkit.inventory.meta.trim.TrimPattern {
    private final NamespacedKey key;
    private final ArmorTrimPattern handle;
    public CraftTrimPattern(NamespacedKey key, ArmorTrimPattern handle) {
        this.key = java.util.Objects.requireNonNull(key); this.handle = java.util.Objects.requireNonNull(handle);
    }
    @Override public NamespacedKey getKey() { return key; }
    public ArmorTrimPattern getHandle() { return handle; }
}
