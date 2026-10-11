package org.bukkit.craftbukkit.command;

import java.util.Set;
import java.util.UUID;
import net.minecraft.server.command.ServerCommandSource;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ProxiedCommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

/** Command sender retaining both the native source and Bukkit caller/callee identities. */
public class ProxiedNativeCommandSender implements ProxiedCommandSender {
    private final ServerCommandSource handle;
    private final CommandSender caller;
    private final CommandSender callee;

    public ProxiedNativeCommandSender(ServerCommandSource handle, CommandSender caller, CommandSender callee) {
        this.handle = java.util.Objects.requireNonNull(handle, "handle");
        this.caller = java.util.Objects.requireNonNull(caller, "caller");
        this.callee = java.util.Objects.requireNonNull(callee, "callee");
    }

    public ServerCommandSource getHandle() { return handle; }
    @Override public CommandSender getCaller() { return caller; }
    @Override public CommandSender getCallee() { return callee; }
    @Override public void sendMessage(String message) { caller.sendMessage(message); }
    @Override public void sendMessage(String... messages) { caller.sendMessage(messages); }
    @Override public void sendMessage(UUID sender, String message) { caller.sendMessage(sender, message); }
    @Override public void sendMessage(UUID sender, String... messages) { caller.sendMessage(sender, messages); }
    @Override public Server getServer() { return callee.getServer(); }
    @Override public String getName() { return callee.getName(); }
    @Override public net.kyori.adventure.text.Component name() { return callee.name(); }
    @Override public boolean isPermissionSet(String name) { return caller.isPermissionSet(name); }
    @Override public boolean isPermissionSet(Permission permission) { return caller.isPermissionSet(permission); }
    @Override public boolean hasPermission(String name) { return caller.hasPermission(name); }
    @Override public boolean hasPermission(Permission permission) { return caller.hasPermission(permission); }
    @Override public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) { return caller.addAttachment(plugin, name, value); }
    @Override public PermissionAttachment addAttachment(Plugin plugin) { return caller.addAttachment(plugin); }
    @Override public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) { return caller.addAttachment(plugin, name, value, ticks); }
    @Override public PermissionAttachment addAttachment(Plugin plugin, int ticks) { return caller.addAttachment(plugin, ticks); }
    @Override public void removeAttachment(PermissionAttachment attachment) { caller.removeAttachment(attachment); }
    @Override public void recalculatePermissions() { caller.recalculatePermissions(); }
    @Override public Set<PermissionAttachmentInfo> getEffectivePermissions() { return caller.getEffectivePermissions(); }
    @Override public boolean isOp() { return caller.isOp(); }
    @Override public void setOp(boolean value) { caller.setOp(value); }
    @Override public CommandSender.Spigot spigot() { return caller.spigot(); }
}
