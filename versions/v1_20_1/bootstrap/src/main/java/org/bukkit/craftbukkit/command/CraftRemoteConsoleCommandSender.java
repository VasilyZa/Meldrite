package org.bukkit.craftbukkit.command;

import java.net.SocketAddress;
import org.bukkit.command.CommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;

/** RCON-compatible sender facade; transport ownership stays with the native bridge. */
public class CraftRemoteConsoleCommandSender extends ServerCommandSender implements RemoteConsoleCommandSender {
    private final CommandSender delegate;
    private final SocketAddress address;
    public CraftRemoteConsoleCommandSender(CommandSender delegate, SocketAddress address) {
        this.delegate = java.util.Objects.requireNonNull(delegate, "delegate");
        this.address = address;
    }
    public CommandSender getDelegate() { return delegate; }
    @Override public SocketAddress getAddress() { return address; }
    @Override public void sendMessage(String message) { delegate.sendMessage(message); }
    @Override public void sendMessage(String... messages) { delegate.sendMessage(messages); }
    @Override public String getName() { return "Rcon"; }
    @Override public boolean isOp() { return true; }
    @Override public void setOp(boolean value) { throw new UnsupportedOperationException("Cannot change operator status of remote controller"); }
    @Override public boolean hasPermission(String name) { return true; }
    @Override public boolean hasPermission(org.bukkit.permissions.Permission permission) { return true; }
    public net.minecraft.server.command.ServerCommandSource getListener() {
        if (getServer() instanceof org.bukkit.craftbukkit.v1_20_R1.CraftServer craft) return craft.getServer().getCommandSource();
        throw new IllegalStateException("Remote console is not attached to the native server");
    }
}
