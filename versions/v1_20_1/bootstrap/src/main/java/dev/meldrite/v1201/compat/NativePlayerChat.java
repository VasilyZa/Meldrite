package dev.meldrite.v1201.compat;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AbstractChatEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.event.player.ChatEvent;
import java.util.LinkedHashSet;
import java.util.Set;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

/** Handles explicit Bukkit Player.chat calls; native Fabric network chat keeps its own path. */
public final class NativePlayerChat {
    private static final String DEFAULT_LEGACY_FORMAT = "<%1$s> %2$s";
    private NativePlayerChat() {}

    public static void command(Player player, String message) {
        var server = player.getServer();
        var event = new PlayerCommandPreprocessEvent(player, message, new LinkedHashSet<>(server.getOnlinePlayers()));
        server.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;
        try {
            server.dispatchCommand(event.getPlayer(), event.getMessage().substring(1));
        } catch (org.bukkit.command.CommandException error) {
            event.getPlayer().sendMessage("\u00a7cAn internal error occurred while attempting to perform this command");
            server.getLogger().log(java.util.logging.Level.SEVERE, "Could not dispatch player command " + event.getMessage(), error);
        }
    }

    @SuppressWarnings("deprecation")
    public static void message(Player player, String message) {
        var server = player.getServer();
        Component original = Component.text(message);
        Component current = original;
        Player sender = player;
        boolean cancelled = false;
        ChatRenderer renderer = ChatRenderer.defaultRenderer();
        Set<Audience> viewers = new LinkedHashSet<>(server.getOnlinePlayers());
        if (hasListeners(AsyncPlayerChatEvent.getHandlerList()) || hasListeners(PlayerChatEvent.getHandlerList())) {
            var async = new AsyncPlayerChatEvent(false, player, message, new LinkedHashSet<>(server.getOnlinePlayers()));
            server.getPluginManager().callEvent(async);
            String legacyMessage = async.getMessage();
            String format = async.getFormat();
            Set<Player> recipients = async.getRecipients();
            sender = async.getPlayer();
            cancelled = async.isCancelled();
            if (hasListeners(PlayerChatEvent.getHandlerList())) {
                var sync = new PlayerChatEvent(sender, legacyMessage, format, recipients);
                sync.setCancelled(cancelled);
                server.getPluginManager().callEvent(sync);
                legacyMessage = sync.getMessage();
                format = sync.getFormat();
                recipients = sync.getRecipients();
                sender = sync.getPlayer();
                cancelled = sync.isCancelled();
            }
            if (!message.equals(legacyMessage)) current = LegacyComponentSerializer.legacySection().deserialize(legacyMessage);
            if (!DEFAULT_LEGACY_FORMAT.equals(format)) {
                String changedFormat = format;
                renderer = ChatRenderer.viewerUnaware((source, displayName, content) -> LegacyComponentSerializer.legacySection().deserialize(
                        String.format(changedFormat, source.getDisplayName(), LegacyComponentSerializer.legacySection().serialize(content))));
            }
            viewers = new LinkedHashSet<>(recipients);
        }
        viewers.add(server.getConsoleSender());
        SignedMessage signed = SignedMessage.system(message, original);
        var async = new AsyncChatEvent(false, sender, viewers, renderer, current, original, signed);
        async.setCancelled(cancelled);
        server.getPluginManager().callEvent(async);
        AbstractChatEvent result = async;
        if (hasListeners(ChatEvent.getHandlerList())) {
            var sync = new ChatEvent(sender, async.viewers(), async.renderer(), async.message(), original, signed);
            sync.setCancelled(async.isCancelled());
            server.getPluginManager().callEvent(sync);
            result = sync;
        }
        if (result.isCancelled()) return;
        Player source = result.getPlayer();
        Component displayName = source.displayName();
        Identity identity = Identity.identity(source.getUniqueId());
        for (Audience viewer : result.viewers()) {
            sendToAudience(viewer, identity, result.renderer().render(source, displayName, result.message(), viewer));
        }
    }

    private static void sendToAudience(Audience viewer, Identity sender, Component message) {
        if (viewer instanceof org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer player
                && player.getHandle().getClientChatVisibility() != net.minecraft.client.option.ChatVisibility.FULL) return;
        if (viewer instanceof net.kyori.adventure.audience.ForwardingAudience forwarding) {
            for (Audience audience : forwarding.audiences()) sendToAudience(audience, sender, message);
        } else viewer.sendMessage(sender, message, MessageType.CHAT);
    }

    private static boolean hasListeners(HandlerList handlers) {
        return handlers.getRegisteredListeners().length != 0;
    }
}
