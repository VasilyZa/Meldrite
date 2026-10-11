package org.bukkit.craftbukkit;

/** Unversioned ABI entry point for native effect payload conversion. */
public final class CraftEffect {
    private CraftEffect() { }
    public static <T> int getDataValue(org.bukkit.Effect effect, T data) {
        return org.bukkit.craftbukkit.v1_20_R1.CraftEffect.getDataValue(effect, data);
    }
}
