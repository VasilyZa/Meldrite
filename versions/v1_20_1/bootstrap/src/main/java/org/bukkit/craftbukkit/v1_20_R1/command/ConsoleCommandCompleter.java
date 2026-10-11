package org.bukkit.craftbukkit.v1_20_R1.command;

import java.util.List;
import java.util.Objects;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;

/** Console completion bridge that uses the live Bukkit command map. */
public final class ConsoleCommandCompleter {
    private final MinecraftDedicatedServer server;
    public ConsoleCommandCompleter(MinecraftDedicatedServer server) { this.server = Objects.requireNonNull(server); }
    public List<String> complete(String buffer) {
        org.bukkit.craftbukkit.v1_20_R1.CraftServer craft = (org.bukkit.craftbukkit.v1_20_R1.CraftServer) org.bukkit.Bukkit.getServer();
        List<String> result = craft.getCommandMap().tabComplete(craft.getConsoleSender(), buffer == null ? "" : buffer);
        return result == null ? List.of() : List.copyOf(result);
    }
    public MinecraftDedicatedServer getServer() { return server; }
}
