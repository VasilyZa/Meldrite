package org.bukkit.craftbukkit.v1_20_R1.generator.structure;

import net.minecraft.registry.Registries;
import net.minecraft.world.gen.structure.Structure;
import org.bukkit.NamespacedKey;

public final class CraftStructure extends org.bukkit.generator.structure.Structure {
    private final NamespacedKey key;
    private final Structure handle;
    public CraftStructure(NamespacedKey key, Structure handle) {
        this.key = java.util.Objects.requireNonNull(key); this.handle = java.util.Objects.requireNonNull(handle);
    }
    @Override public NamespacedKey getKey() { return key; }
    public Structure getHandle() { return handle; }
    @Override public org.bukkit.generator.structure.StructureType getStructureType() {
        var id = java.util.Objects.requireNonNull(Registries.STRUCTURE_TYPE.getId(handle.getType()));
        return org.bukkit.Registry.STRUCTURE_TYPE.get(new NamespacedKey(id.getNamespace(), id.getPath()));
    }
}
