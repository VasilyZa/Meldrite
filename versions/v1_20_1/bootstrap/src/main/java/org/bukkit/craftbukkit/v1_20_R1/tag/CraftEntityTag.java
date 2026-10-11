package org.bukkit.craftbukkit.v1_20_R1.tag;

import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public final class CraftEntityTag extends CraftTag<EntityType<?>, org.bukkit.entity.EntityType> {
    public CraftEntityTag(Registry<EntityType<?>> registry, TagKey<EntityType<?>> tag) { super(registry, tag); }
    @Override protected EntityType<?> toNative(org.bukkit.entity.EntityType type) {
        if (type == org.bukkit.entity.EntityType.UNKNOWN) return null;
        return registry.getOrEmpty(new Identifier(type.getKey().getNamespace(), type.getKey().getKey())).orElse(null);
    }
    @Override protected org.bukkit.entity.EntityType toBukkit(EntityType<?> type) {
        var id = registry.getId(type);
        var value = org.bukkit.Registry.ENTITY_TYPE.get(new org.bukkit.NamespacedKey(id.getNamespace(), id.getPath()));
        if (value == null) throw new UnsupportedOperationException("UNREPRESENTABLE_NATIVE_ENTITY_TAG_MEMBER: " + id);
        return value;
    }
}
