package dev.meldrite.v1201.bridge;

import dev.meldrite.bridge.RegistryPort;
import dev.meldrite.foundation.RegistryEpoch;
import dev.meldrite.foundation.ResourceKey;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Explicit registry projection for the 1.20.1 composition.
 *
 * Static entries are copied once. Reloadable entries are replaced by
 * {@link #replaceReloadable(Set)} and listeners are notified only after the
 * new snapshot is installed. The bridge never reconstructs native registries.
 */
public final class RegistryBridge implements RegistryPort {
    private final Map<String, Set<ResourceKey>> scopes = new LinkedHashMap<>();
    private final Set<Consumer<ResourceKey>> listeners = new LinkedHashSet<>();

    public RegistryBridge(Map<String, ? extends Set<ResourceKey>> initial) {
        Objects.requireNonNull(initial, "initial");
        initial.forEach((scope, values) -> scopes.put(requireScope(scope), Set.copyOf(values)));
    }

    public synchronized void replaceReloadable(Set<ResourceKey> values) {
        Objects.requireNonNull(values, "values");
        Set<ResourceKey> next = Set.copyOf(values);
        scopes.put("reloadable", next);
        for (ResourceKey key : next) for (Consumer<ResourceKey> listener : Set.copyOf(listeners)) listener.accept(key);
    }

    @Override public synchronized Optional<ResourceKey> resolve(ResourceKey key, RegistryEpoch epoch) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(epoch, "epoch");
        return scopes.values().stream().flatMap(Set::stream).filter(key::equals).findFirst();
    }

    @Override public synchronized Set<ResourceKey> snapshot(String scope) {
        return Set.copyOf(scopes.getOrDefault(requireScope(scope), Set.of()));
    }

    @Override public synchronized AutoCloseable lifecycle(Consumer<ResourceKey> listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
        return () -> { synchronized (RegistryBridge.this) { listeners.remove(listener); } };
    }

    public synchronized Set<String> scopes() { return Set.copyOf(scopes.keySet()); }

    private static String requireScope(String scope) {
        Objects.requireNonNull(scope, "scope");
        if (scope.isBlank()) throw new IllegalArgumentException("scope must not be blank");
        return scope;
    }
}
