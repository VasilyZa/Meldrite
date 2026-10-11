package org.bukkit.craftbukkit.command;

import net.minecraft.server.command.ServerCommandSource;
import org.bukkit.command.CommandSender;

/** Bukkit command identity for a native entity that is not itself a Player. */
public final class CraftEntityCommandSender extends ServerCommandSender {
    private final ServerCommandSource source;

    public CraftEntityCommandSender(ServerCommandSource source) {
        this.source = java.util.Objects.requireNonNull(source, "source");
    }

    public ServerCommandSource getHandle() { return source; }
    @Override public void sendMessage(String message) {
        for (net.minecraft.text.Text line : org.bukkit.craftbukkit.util.CraftChatMessage.fromString(message)) source.sendMessage(line);
    }
    @Override public void sendMessage(String... messages) { if (messages != null) for (String message : messages) sendMessage(message); }
    @Override public String getName() { return source.getName(); }
    @Override public net.kyori.adventure.text.Component name() { return net.kyori.adventure.text.Component.text(source.getDisplayName().getString()); }
    @Override public boolean isOp() { return source.hasPermissionLevel(2); }
    @Override public void setOp(boolean value) { throw new UnsupportedOperationException("Cannot change operator status of a native entity"); }
    @Override public boolean hasPermission(String name) { return source.hasPermissionLevel(2); }
    @Override public boolean hasPermission(org.bukkit.permissions.Permission permission) { return source.hasPermissionLevel(2); }
    @Override public CommandSender.Spigot spigot() { return super.spigot(); }
}
