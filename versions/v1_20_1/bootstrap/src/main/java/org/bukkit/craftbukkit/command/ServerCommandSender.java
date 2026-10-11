package org.bukkit.craftbukkit.command;

import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

/** Shared permission and server plumbing for non-player command senders. */
public abstract class ServerCommandSender implements CommandSender {
    protected final PermissibleBase perm;
    protected ServerCommandSender() { this(new PermissibleBase(null)); }
    protected ServerCommandSender(PermissibleBase permissible) { this.perm = permissible == null ? new PermissibleBase(this) : permissible; }
    @Override public boolean isPermissionSet(String name) { return perm.isPermissionSet(name); }
    @Override public boolean isPermissionSet(Permission permission) { return perm.isPermissionSet(permission); }
    @Override public boolean hasPermission(String name) { return perm.hasPermission(name); }
    @Override public boolean hasPermission(Permission permission) { return perm.hasPermission(permission); }
    @Override public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) { return perm.addAttachment(plugin, name, value); }
    @Override public PermissionAttachment addAttachment(Plugin plugin) { return perm.addAttachment(plugin); }
    @Override public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) { return perm.addAttachment(plugin, name, value, ticks); }
    @Override public PermissionAttachment addAttachment(Plugin plugin, int ticks) { return perm.addAttachment(plugin, ticks); }
    @Override public void removeAttachment(PermissionAttachment attachment) { perm.removeAttachment(attachment); }
    @Override public void recalculatePermissions() { perm.recalculatePermissions(); }
    @Override public Set<PermissionAttachmentInfo> getEffectivePermissions() { return perm.getEffectivePermissions(); }
    @Override public Server getServer() { return Bukkit.getServer(); }
    @Override public void sendMessage(UUID sender, String message) { sendMessage(message); }
    @Override public void sendMessage(UUID sender, String... messages) { sendMessage(messages); }
    @Override public CommandSender.Spigot spigot() {
        return new CommandSender.Spigot() {
            @Override public void sendMessage(net.md_5.bungee.api.chat.BaseComponent component) { if (component != null) ServerCommandSender.this.sendMessage(net.md_5.bungee.api.chat.TextComponent.toLegacyText(component)); }
            @Override public void sendMessage(net.md_5.bungee.api.chat.BaseComponent... components) { if (components != null) ServerCommandSender.this.sendMessage(net.md_5.bungee.api.chat.TextComponent.toLegacyText(components)); }
            @Override public void sendMessage(UUID sender, net.md_5.bungee.api.chat.BaseComponent component) { sendMessage(component); }
            @Override public void sendMessage(UUID sender, net.md_5.bungee.api.chat.BaseComponent... components) { sendMessage(components); }
        };
    }
    @Override public net.kyori.adventure.text.Component name() { return net.kyori.adventure.text.Component.text(getName()); }
}
