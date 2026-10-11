package org.bukkit.craftbukkit.v1_20_R1.util;

import java.util.HashSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.bukkit.craftbukkit.util.LazyHashSet;
import org.bukkit.entity.Player;

/** A live player snapshot which is only materialized when a caller reads it. */
public class LazyPlayerSet extends LazyHashSet<Player> {
    private final MinecraftServer server;
    public LazyPlayerSet(MinecraftServer server) { this.server = java.util.Objects.requireNonNull(server, "server"); }
    @Override protected java.util.Set<Player> makeReference() { return makePlayerSet(server); }
    public static HashSet<Player> makePlayerSet(MinecraftServer server) {
        var players = server.getPlayerManager().getPlayerList();
        HashSet<Player> result = new HashSet<>(players.size());
        for (ServerPlayerEntity player : players) result.add((Player) dev.meldrite.v1201.compat.NativeEntityExtensions.bukkit(player));
        return result;
    }
}
