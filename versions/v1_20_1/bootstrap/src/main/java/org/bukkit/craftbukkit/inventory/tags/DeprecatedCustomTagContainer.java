package org.bukkit.craftbukkit.inventory.tags;

import java.util.Objects;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.tags.CustomItemTagContainer;
import org.bukkit.inventory.meta.tags.ItemTagAdapterContext;
import org.bukkit.inventory.meta.tags.ItemTagType;
import org.bukkit.persistence.PersistentDataContainer;

@SuppressWarnings("unchecked")
public final class DeprecatedCustomTagContainer implements CustomItemTagContainer {
    private final PersistentDataContainer wrapped;

    public DeprecatedCustomTagContainer(PersistentDataContainer wrapped) {
        this.wrapped = Objects.requireNonNull(wrapped, "wrapped");
    }

    @Override public <T, Z> void setCustomTag(NamespacedKey key, ItemTagType<T, Z> type, Z value) {
        if (Objects.equals(CustomItemTagContainer.class, type.getPrimitiveType())) {
            wrapped.set(key, new DeprecatedContainerTagType<>((ItemTagType<CustomItemTagContainer, Z>) type), value);
        } else {
            wrapped.set(key, new DeprecatedItemTagType<>(type), value);
        }
    }

    @Override public <T, Z> boolean hasCustomTag(NamespacedKey key, ItemTagType<T, Z> type) {
        return Objects.equals(CustomItemTagContainer.class, type.getPrimitiveType())
                ? wrapped.has(key, new DeprecatedContainerTagType<>((ItemTagType<CustomItemTagContainer, Z>) type))
                : wrapped.has(key, new DeprecatedItemTagType<>(type));
    }

    @Override public <T, Z> Z getCustomTag(NamespacedKey key, ItemTagType<T, Z> type) {
        return Objects.equals(CustomItemTagContainer.class, type.getPrimitiveType())
                ? wrapped.get(key, new DeprecatedContainerTagType<>((ItemTagType<CustomItemTagContainer, Z>) type))
                : wrapped.get(key, new DeprecatedItemTagType<>(type));
    }

    @Override public void removeCustomTag(NamespacedKey key) { wrapped.remove(key); }
    @Override public boolean isEmpty() { return wrapped.isEmpty(); }
    @Override public ItemTagAdapterContext getAdapterContext() { return new DeprecatedItemAdapterContext(wrapped.getAdapterContext()); }
    public PersistentDataContainer getWrapped() { return wrapped; }
}
