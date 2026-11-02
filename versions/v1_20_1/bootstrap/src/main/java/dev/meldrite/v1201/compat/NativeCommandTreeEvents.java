package dev.meldrite.v1201.compat;

import com.destroystokyo.paper.event.brigadier.AsyncPlayerSendCommandsEvent;
import com.destroystokyo.paper.event.brigadier.AsyncPlayerSendSuggestionsEvent;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.RootCommandNode;
import java.util.LinkedHashSet;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.bukkit.craftbukkit.command.BukkitCommandTree;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandSendEvent;

/** Paper's synchronous command-view path; the native dispatcher remains authoritative. */
public final class NativeCommandTreeEvents {
    private NativeCommandTreeEvents() { }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void prepare(ServerPlayerEntity player, RootCommandNode<CommandSource> root) {
        if (!NativeEntityExtensions.eventsAvailable()) return;
        if (!player.server.isOnThread()) throw new IllegalStateException("ASYNC_COMMAND_TREE_SEND");
        var facade = (Player) NativeEntityExtensions.bukkit(player);
        var plugins = facade.getServer().getPluginManager();
        // Paper snapshots these names before plugins add nodes to the client view.
        var originalNames = new LinkedHashSet<String>();
        root.getChildren().forEach(node -> originalNames.add(node.getName()));
        var originalProviders = NativeCommandSuggestions.providers(root);
        // The documented synchronous fallback fires once and has no earlier async phase.
        plugins.callEvent(new AsyncPlayerSendCommandsEvent(facade, (RootCommandNode) root, false));
        var names = new LinkedHashSet<>(originalNames);
        plugins.callEvent(new PlayerCommandSendEvent(facade, names));
        for (String name : originalNames) if (!names.contains(name)) BukkitCommandTree.remove(root, name);
        NativeCommandSuggestions.capture(player, root, originalProviders);
    }

    public static void sendSuggestions(ServerPlayerEntity player, int requestId, String buffer, Suggestions suggestions) {
        Runnable send = () -> {
            if (org.spigotmc.SpigotConfig.tabComplete < 0) return;
            if (player.networkHandler == null || !player.networkHandler.isConnectionOpen()) return;
            var facade = (Player) NativeEntityExtensions.bukkit(player);
            Suggestions visible = suggestions;
            if (!org.spigotmc.SpigotConfig.sendNamespaced && suggestions.getRange().getStart() <= 1) {
                visible = new Suggestions(suggestions.getRange(), suggestions.getList().stream()
                        .filter(suggestion -> !suggestion.getText().contains(":"))
                        .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
            }
            var event = new AsyncPlayerSendSuggestionsEvent(facade, visible, buffer);
            event.setCancelled(visible.isEmpty());
            facade.getServer().getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                player.networkHandler.sendPacket(new CommandSuggestionsS2CPacket(requestId,
                        java.util.Objects.requireNonNull(event.getSuggestions(), "Plugin suggestions cannot be null")));
            }
        };
        // Native suggestion futures may finish off-thread. Do not run synchronous plugin hooks there.
        if (player.server.isOnThread()) send.run(); else player.server.execute(send);
    }
}
