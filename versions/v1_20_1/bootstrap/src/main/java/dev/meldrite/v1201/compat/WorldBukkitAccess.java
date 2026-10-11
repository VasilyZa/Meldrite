package dev.meldrite.v1201.compat;

import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;

/** Native world bridge used by Bukkit-compatible reflection consumers. */
public interface WorldBukkitAccess {
    CraftWorld meldrite$getBukkitWorld();
    void meldrite$setBukkitWorld(CraftWorld world);
}
