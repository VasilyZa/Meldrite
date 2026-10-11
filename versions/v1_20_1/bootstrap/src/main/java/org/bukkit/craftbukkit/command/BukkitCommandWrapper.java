package org.bukkit.craftbukkit.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import net.minecraft.server.command.ServerCommandSource;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;

/** Exposes a Bukkit command as a Brigadier literal and greedy argument. */
public class BukkitCommandWrapper implements com.mojang.brigadier.Command<ServerCommandSource>,
        Predicate<ServerCommandSource>, com.mojang.brigadier.suggestion.SuggestionProvider<ServerCommandSource> {
    private final CraftServer server;
    private final Command command;

    public BukkitCommandWrapper(CraftServer server, Command command) {
        this.server = java.util.Objects.requireNonNull(server, "server");
        this.command = java.util.Objects.requireNonNull(command, "command");
    }

    boolean wraps(Command candidate) { return command == candidate; }

    public LiteralCommandNode<ServerCommandSource> register(CommandDispatcher<ServerCommandSource> dispatcher, String label) {
        LiteralArgumentBuilder<ServerCommandSource> literal = LiteralArgumentBuilder.<ServerCommandSource>literal(label)
                .requires(this).executes(this);
        literal.then(RequiredArgumentBuilder.<ServerCommandSource, String>argument("args", StringArgumentType.greedyString())
                .suggests(this).executes(this));
        LiteralCommandNode<ServerCommandSource> node = literal.build();
        dispatcher.getRoot().addChild(node);
        return node;
    }

    @Override public boolean test(ServerCommandSource source) { return command.testPermissionSilent(toBukkit(source)); }

    @Override public int run(CommandContext<ServerCommandSource> context) {
        CommandSender sender = toBukkit(context.getSource());
        try {
            return server.dispatchCommand(sender, context.getInput().substring(context.getRange().getStart())) ? 1 : 0;
        } catch (CommandException failure) {
            sender.sendMessage(org.bukkit.ChatColor.RED + "An internal error occurred while attempting to perform this command");
            server.getLogger().log(java.util.logging.Level.SEVERE, "Error executing Bukkit command", failure);
            return 0;
        }
    }

    @Override public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        String input = builder.getInput();
        var source = context.getSource();
        var position = source.getPosition();
        var world = source.getWorld();
        var location = world == null ? null : new org.bukkit.Location(server.getWorld(org.bukkit.NamespacedKey.fromString(world.getRegistryKey().getValue().toString())),
                position.x, position.y, position.z);
        List<String> values = server.getCommandMap().tabComplete(toBukkit(source), input.substring(context.getRange().getStart()), location);
        SuggestionsBuilder target = builder.createOffset(input.lastIndexOf(' ') + 1);
        if (values != null) for (String value : values) target.suggest(value);
        return target.buildFuture();
    }

    private CommandSender toBukkit(ServerCommandSource source) {
        if (source.getEntity() != null) {
            Object bukkit = dev.meldrite.v1201.compat.NativeEntityExtensions.bukkit(source.getEntity());
            if (bukkit instanceof CommandSender sender) return sender;
            return new CraftEntityCommandSender(source);
        }
        return server.getConsoleSender();
    }
}
