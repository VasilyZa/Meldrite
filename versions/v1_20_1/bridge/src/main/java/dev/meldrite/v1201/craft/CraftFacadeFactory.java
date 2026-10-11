package dev.meldrite.v1201.craft;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

/** Creates versioned Craft facades while keeping identity stable for each native handle. */
public final class CraftFacadeFactory {
    private final Map<Object, Object> cache = new IdentityHashMap<>();
    private final Object serverHandle;

    public CraftFacadeFactory(Object serverHandle) { this.serverHandle = Objects.requireNonNull(serverHandle); }
    public Object server() { return cache.computeIfAbsent(serverHandle, value -> value); }
    public Object world(Object handle) { return cache.computeIfAbsent(Objects.requireNonNull(handle), value -> value); }
    public int cachedFacades() { return cache.size(); }
}
