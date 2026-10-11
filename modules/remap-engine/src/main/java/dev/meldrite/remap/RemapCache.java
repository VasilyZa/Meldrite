package dev.meldrite.remap;

import java.util.Map;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

public final class RemapCache {
    public enum Status { HIT, MISS, REBUILD }
    private final Map<String, String> entries = new ConcurrentHashMap<>();
    public Status check(String plugin, MappingBundle mapping, byte[] input) {
        String key = plugin + ":" + Integer.toHexString(Arrays.hashCode(input));
        String fingerprint = mapping.mappingHash() + ":" + mapping.hierarchyHash() + ":" + mapping.transformerVersion();
        String previous = entries.put(key, fingerprint);
        if (previous == null) return Status.MISS;
        return previous.equals(fingerprint) ? Status.HIT : Status.REBUILD;
    }
    public void invalidate(String plugin) { entries.keySet().removeIf(key -> key.startsWith(plugin + ":")); }

}
