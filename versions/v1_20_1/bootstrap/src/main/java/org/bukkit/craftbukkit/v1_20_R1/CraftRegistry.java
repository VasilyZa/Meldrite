package org.bukkit.craftbukkit.v1_20_R1;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;

/** A lazy, identity-preserving view of a live native registry. */
public final class CraftRegistry<B extends Keyed, M> implements org.bukkit.Registry<B> {
    private final net.minecraft.registry.Registry<M> handle;
    private final BiFunction<NamespacedKey, M, B> projection;
    private final Map<NamespacedKey, B> cache = new HashMap<>();
    public CraftRegistry(net.minecraft.registry.Registry<M> handle, BiFunction<NamespacedKey, M, B> projection) {
        this.handle = Objects.requireNonNull(handle);
        this.projection = Objects.requireNonNull(projection);
    }
    public net.minecraft.registry.Registry<M> getHandle() { return handle; }
    @Override public synchronized B get(NamespacedKey key) {
        Objects.requireNonNull(key);
        B cached = cache.get(key);
        if (cached != null) return cached;
        M nativeValue = handle.getOrEmpty(new Identifier(key.getNamespace(), key.getKey())).orElse(null);
        if (nativeValue == null) return null;
        B projected = projection.apply(key, nativeValue);
        // Bukkit's static constants can reenter this registry while their class initializes.
        B initialized = cache.get(key);
        if (initialized != null) return initialized;
        cache.put(key, projected);
        return projected;
    }
    @Override public Stream<B> stream() {
        return handle.getIds().stream().map(id -> get(new NamespacedKey(id.getNamespace(), id.getPath())));
    }
    @Override public Iterator<B> iterator() { return stream().iterator(); }
}
