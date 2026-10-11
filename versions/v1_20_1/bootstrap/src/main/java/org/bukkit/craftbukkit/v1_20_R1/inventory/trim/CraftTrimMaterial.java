package org.bukkit.craftbukkit.v1_20_R1.inventory.trim;

import net.minecraft.item.trim.ArmorTrimMaterial;
import org.bukkit.NamespacedKey;

public final class CraftTrimMaterial implements org.bukkit.inventory.meta.trim.TrimMaterial {
    private final NamespacedKey key;
    private final ArmorTrimMaterial handle;
    public CraftTrimMaterial(NamespacedKey key, ArmorTrimMaterial handle) {
        this.key = java.util.Objects.requireNonNull(key); this.handle = java.util.Objects.requireNonNull(handle);
    }
    @Override public NamespacedKey getKey() { return key; }
    public ArmorTrimMaterial getHandle() { return handle; }
}
