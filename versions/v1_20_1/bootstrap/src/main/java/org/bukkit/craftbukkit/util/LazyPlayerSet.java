package org.bukkit.craftbukkit.util;

import java.util.HashSet;
import net.minecraft.server.MinecraftServer;
import org.bukkit.entity.Player;

/** Legacy package entry point for the live player set used by CraftBukkit. */
public class LazyPlayerSet extends org.bukkit.craftbukkit.v1_20_R1.util.LazyPlayerSet {
    public LazyPlayerSet(MinecraftServer server) { super(server); }

    public static HashSet<Player> makePlayerSet(MinecraftServer server) {
        return org.bukkit.craftbukkit.v1_20_R1.util.LazyPlayerSet.makePlayerSet(server);
    }
}
