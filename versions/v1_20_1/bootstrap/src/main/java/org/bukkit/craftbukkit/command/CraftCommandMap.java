package org.bukkit.craftbukkit.command;

import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.defaults.PluginsCommand;

/** Exposes the same known-command map that CraftBukkit plugins inspect. */
public class CraftCommandMap extends SimpleCommandMap {
    private final Map<String, VanillaCommandWrapper> nativeCommands = new LinkedHashMap<>();
    private CommandManager nativeDispatcher;

    public CraftCommandMap(Server server) {
        super(server);
        register("bukkit", new PluginsCommand("plugins"));
    }
    @Override public synchronized boolean register(String label, String fallbackPrefix, Command command) {
        // The superclass registers default commands before this class's fields are initialized.
        if (nativeCommands == null || nativeCommands.isEmpty()) return super.register(label, fallbackPrefix, command);
        var labels = new ArrayList<>(command.getAliases());
        labels.add(label);
        Map<String, VanillaCommandWrapper> displaced = new LinkedHashMap<>();
        for (String name : labels) {
            String key = name.toLowerCase(Locale.ENGLISH).trim();
            VanillaCommandWrapper nativeCommand = nativeCommands.get(key);
            if (nativeCommand != null && knownCommands.remove(key, nativeCommand)) displaced.put(key, nativeCommand);
        }
        try {
            return super.register(label, fallbackPrefix, command);
        } finally {
            displaced.forEach((key, nativeCommand) -> {
                if (!knownCommands.containsKey(key)) knownCommands.put(key, nativeCommand);
                else {
                    nativeCommands.remove(key, nativeCommand);
                    nativeCommand.unregister(this);
                }
            });
        }
    }

    @Override public synchronized void clearCommands() {
        super.clearCommands();
        nativeCommands.clear();
        nativeDispatcher = null;
        register("bukkit", new PluginsCommand("plugins"));
    }
    public Map<String, Command> getKnownCommands() { return knownCommands; }

    /** Bukkit commands to publish into Brigadier, excluding routes that already originate there. */
    public synchronized Map<String, Command> getBukkitCommands() {
        Map<String, Command> commands = new LinkedHashMap<>(knownCommands);
        nativeCommands.forEach((label, command) -> commands.remove(label, command));
        return commands;
    }

    /** Mirrors native roots into the command map without replacing existing Bukkit routes. */
    public synchronized void synchronizeNativeCommands(CommandManager dispatcher, Predicate<CommandNode<ServerCommandSource>> isBukkitOrigin) {
        var root = dispatcher.getDispatcher().getRoot();
        nativeCommands.entrySet().removeIf(entry -> {
            VanillaCommandWrapper command = entry.getValue();
            if (nativeDispatcher == dispatcher && root.getChild(command.vanillaCommand.getName()) == command.vanillaCommand
                    && knownCommands.get(entry.getKey()) == command && !isBukkitOrigin.test(command.vanillaCommand)) return false;
            knownCommands.remove(entry.getKey(), command);
            command.unregister(this);
            return true;
        });
        nativeDispatcher = dispatcher;
        for (CommandNode<ServerCommandSource> node : root.getChildren()) {
            if (!(node instanceof LiteralCommandNode<?>) || isBukkitOrigin.test(node)) continue;
            String label = node.getName().toLowerCase(Locale.ENGLISH);
            if (knownCommands.containsKey(label)) continue;
            var command = new VanillaCommandWrapper(dispatcher, node);
            if (!command.register(this)) throw new IllegalStateException("NATIVE_COMMAND_REGISTER_REJECTED: " + label);
            knownCommands.put(label, command);
            nativeCommands.put(label, command);
        }
    }
}
