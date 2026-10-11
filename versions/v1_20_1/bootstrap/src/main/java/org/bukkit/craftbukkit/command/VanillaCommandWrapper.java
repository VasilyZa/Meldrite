package org.bukkit.craftbukkit.command;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.tree.CommandNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.ProxiedCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.command.defaults.BukkitCommand;

/** Bridges one Brigadier vanilla node into the Bukkit command map. */
public final class VanillaCommandWrapper extends BukkitCommand {
    private final CommandManager dispatcher;
    public final CommandNode<ServerCommandSource> vanillaCommand;

    public VanillaCommandWrapper(CommandManager dispatcher, CommandNode<ServerCommandSource> vanillaCommand) {
        super(vanillaCommand.getName(), "A Mojang provided command.", vanillaCommand.getUsageText(), Collections.emptyList());
        this.dispatcher = Preconditions.checkNotNull(dispatcher);
        this.vanillaCommand = Preconditions.checkNotNull(vanillaCommand);
        setPermission(getPermission(vanillaCommand));
    }

    @Override public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!testPermission(sender)) return true;
        ServerCommandSource source = getListener(sender);
        dispatcher.executeWithPrefix(source, toDispatcher(args, commandLabel));
        return true;
    }

    @Override public List<String> tabComplete(CommandSender sender, String alias, String[] args, Location location) {
        Preconditions.checkNotNull(sender, "Sender cannot be null");
        Preconditions.checkNotNull(args, "Arguments cannot be null");
        String input = toDispatcher(args, getName());
        ParseResults<ServerCommandSource> parsed = dispatcher.getDispatcher().parse(input, getListener(sender));
        List<String> result = new ArrayList<>();
        dispatcher.getDispatcher().getCompletionSuggestions(parsed).join().getList().forEach(s -> result.add(s.getText()));
        return result;
    }

    public static ServerCommandSource getListener(CommandSender sender) {
        if (sender instanceof ProxiedNativeCommandSender proxied) return proxied.getHandle();
        if (sender instanceof CraftEntityCommandSender entity) return entity.getHandle();
        if (sender instanceof BlockCommandSender block && block instanceof CraftBlockCommandSender craft) return craft.getWrapper();
        if (sender instanceof RemoteConsoleCommandSender remote && remote instanceof CraftRemoteConsoleCommandSender craft) return craft.getListener();
        if (sender instanceof ConsoleCommandSender && sender.getServer() instanceof org.bukkit.craftbukkit.v1_20_R1.CraftServer craft) {
            return craft.getServer().getCommandSource();
        }
        throw new IllegalArgumentException("Cannot make " + sender + " a vanilla command source");
    }

    public static String getPermission(CommandNode<ServerCommandSource> node) {
        String name = node.getRedirect() == null ? node.getName() : node.getRedirect().getName();
        return "minecraft.command." + stripDefaultNamespace(name);
    }

    private static String stripDefaultNamespace(String name) {
        return name.startsWith("minecraft:") ? name.substring("minecraft:".length()) : name;
    }

    private static String toDispatcher(String[] args, String name) {
        return name + (args.length == 0 ? "" : " " + Joiner.on(' ').join(args));
    }
}
