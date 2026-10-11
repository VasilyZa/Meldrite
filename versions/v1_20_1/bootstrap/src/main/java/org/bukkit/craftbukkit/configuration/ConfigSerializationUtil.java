package org.bukkit.craftbukkit.configuration;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

/** Type-checked accessors used by Bukkit configuration serializers. */
public final class ConfigSerializationUtil {
    private ConfigSerializationUtil() {}

    public static String getString(Map<?, ?> map, String key, boolean nullable) {
        return getObject(String.class, map, key, nullable);
    }

    public static UUID getUuid(Map<?, ?> map, String key, boolean nullable) {
        String value = getString(map, key, nullable);
        return value == null ? null : UUID.fromString(value);
    }

    public static <T> T getObject(Class<T> clazz, Map<?, ?> map, String key, boolean nullable) {
        Object value = map.get(key);
        if (clazz.isInstance(value)) return clazz.cast(value);
        if (value == null) {
            if (!nullable) throw new NoSuchElementException(map + " does not contain " + key);
            return null;
        }
        throw new IllegalArgumentException(key + "(" + value + ") is not a valid " + clazz);
    }
}
