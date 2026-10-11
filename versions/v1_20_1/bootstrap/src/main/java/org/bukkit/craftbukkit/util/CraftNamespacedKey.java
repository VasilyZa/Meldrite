package org.bukkit.craftbukkit.util;

import net.minecraft.util.Identifier;
import org.bukkit.NamespacedKey;

/** Legacy package entry point retained for plugins compiled against CraftBukkit. */
public final class CraftNamespacedKey {
    private CraftNamespacedKey() {}

    public static NamespacedKey fromStringOrNull(String value) {
        return org.bukkit.craftbukkit.v1_20_R1.util.CraftNamespacedKey.fromStringOrNull(value);
    }

    public static NamespacedKey fromString(String value) {
        return org.bukkit.craftbukkit.v1_20_R1.util.CraftNamespacedKey.fromString(value);
    }

    public static NamespacedKey fromMinecraft(Identifier identifier) {
        return org.bukkit.craftbukkit.v1_20_R1.util.CraftNamespacedKey.fromMinecraft(identifier);
    }

    public static Identifier toMinecraft(NamespacedKey key) {
        return org.bukkit.craftbukkit.v1_20_R1.util.CraftNamespacedKey.toMinecraft(key);
    }
}
