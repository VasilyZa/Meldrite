package org.bukkit.craftbukkit.v1_20_R1.tag;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;

public abstract class CraftTag<N, B extends Keyed> implements org.bukkit.Tag<B> {
    protected final Registry<N> registry;
    protected final TagKey<N> tag;
    public CraftTag(Registry<N> registry, TagKey<N> tag) {
        this.registry = Objects.requireNonNull(registry);
        this.tag = Objects.requireNonNull(tag);
        registry.getEntryList(tag).orElseThrow(() -> new IllegalArgumentException("Unknown native tag: " + tag.id()));
    }
    public RegistryEntryList.Named<N> getHandle() { return registry.getEntryList(tag).orElseThrow(); }
    protected abstract N toNative(B value);
    protected abstract B toBukkit(N value);
    @Override public NamespacedKey getKey() { return new NamespacedKey(tag.id().getNamespace(), tag.id().getPath()); }
    @Override public boolean isTagged(B value) {
        N nativeValue = toNative(Objects.requireNonNull(value));
        return nativeValue != null && registry.getEntry(nativeValue).isIn(tag);
    }
    @Override public Set<B> getValues() {
        return getHandle().stream().map(entry -> Objects.requireNonNull(toBukkit(entry.value())))
                .collect(Collectors.toUnmodifiableSet());
    }
}
