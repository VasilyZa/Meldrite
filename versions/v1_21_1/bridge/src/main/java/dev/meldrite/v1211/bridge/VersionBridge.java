package dev.meldrite.v1211.bridge;

import java.util.LinkedHashMap;
import java.util.Map;

/** 1.21.1-owned bridge. Native data is represented without leaking game classes. */
public final class VersionBridge {
    public String minecraftVersion() { return "1.21.1"; }
    public Map<String, Object> preserveNativeData(Map<String, Object> components, Map<String, Object> unknown) {
        Map<String, Object> result = new LinkedHashMap<>(components);
        result.putAll(unknown);
        return Map.copyOf(result);
    }
}
