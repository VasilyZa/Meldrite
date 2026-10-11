package org.bukkit.craftbukkit.v1_20_R1.util;

import net.minecraft.util.Identifier;
import org.bukkit.NamespacedKey;

/** Converts the Bukkit key representation to the native 1.20.1 identifier. */
public final class CraftNamespacedKey {
    private CraftNamespacedKey() {}

    public static NamespacedKey fromStringOrNull(String value) {
        if (value == null || value.isEmpty()) return null;
        Identifier identifier = Identifier.tryParse(value);
        return identifier == null || identifier.getPath().isEmpty() ? null : fromMinecraft(identifier);
    }

    public static NamespacedKey fromString(String value) {
        return fromMinecraft(new Identifier(value));
    }

    public static NamespacedKey fromMinecraft(Identifier identifier) {
        return new NamespacedKey(identifier.getNamespace(), identifier.getPath());
    }

    public static Identifier toMinecraft(NamespacedKey key) {
        return new Identifier(key.getNamespace(), key.getKey());
    }
}
