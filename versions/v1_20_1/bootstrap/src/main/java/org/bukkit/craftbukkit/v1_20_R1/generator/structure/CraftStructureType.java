package org.bukkit.craftbukkit.v1_20_R1.generator.structure;

import net.minecraft.world.gen.structure.StructureType;
import org.bukkit.NamespacedKey;

public final class CraftStructureType extends org.bukkit.generator.structure.StructureType {
    private final NamespacedKey key;
    private final StructureType<?> handle;
    public CraftStructureType(NamespacedKey key, StructureType<?> handle) {
        this.key = java.util.Objects.requireNonNull(key); this.handle = java.util.Objects.requireNonNull(handle);
    }
    @Override public NamespacedKey getKey() { return key; }
    public StructureType<?> getHandle() { return handle; }
}
