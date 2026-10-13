package dev.meldrite.v1201.compat;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AbstractChatEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.event.player.ChatEvent;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.logging.Logger;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SentMessage;
import net.minecraft.server.filter.FilteredMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

/** Shares Bukkit/Paper chat events between Player.chat and validated, filtered native network messages. */
public final class NativePlayerChat {
    private static final String DEFAULT_LEGACY_FORMAT = "<%1$s> %2$s";
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();
    private static final Logger COMMAND_LOGGER = Logger.getLogger("Minecraft");
    private static final ThreadLocal<Prepared> PREPARED = new ThreadLocal<>();
    private record Prepared(ServerPlayerEntity player, net.minecraft.network.message.SignedMessage message, AbstractChatEvent event) { }
    private NativePlayerChat() {}

    /** Records slash-prefixed input before plugin interception, independently of event listeners. */
    public static void logCommand(String playerName, String message) {
        if (org.spigotmc.SpigotConfig.logCommands) COMMAND_LOGGER.info(playerName + " issued server command: " + message);
    }

    /** Input and result omit the slash; null means plugins cancelled execution. */
    public static String preprocessCommand(Player player, String command) {
        var server = player.getServer();
        var event = new PlayerCommandPreprocessEvent(player, "/" + command, new LinkedHashSet<>(server.getOnlinePlayers()));
        server.getPluginManager().callEvent(event);
        return event.isCancelled() ? null : event.getMessage().substring(1);
    }

    public static void command(Player player, String message) {
        var server = player.getServer();
        logCommand(player.getName(), message);
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

    public static void message(Player player, String message) {
        Component original = Component.text(message);
        var result = dispatch(player, message, original, SignedMessage.system(message, original), false);
        if (result.isCancelled()) return;
        Player source = result.getPlayer();
        Component displayName = source.displayName();
        Identity identity = Identity.identity(source.getUniqueId());
        for (Audience viewer : result.viewers()) {
            sendToAudience(viewer, identity, result.renderer().render(source, displayName, result.message(), viewer));
        }
    }

    /** Explicit Player.chat calls run synchronously. Incoming packets use dispatchAsync and keep their message queue pending. */
    public static AbstractChatEvent dispatch(Player player, String message, Component original, SignedMessage signed, boolean asynchronous) {
        if (asynchronous) throw new IllegalArgumentException("Asynchronous chat requires dispatchAsync with a primary executor");
        var state = legacy(new State(player, message, original, signed), false);
        legacySync(state);
        return modernSync(modern(state, false));
    }

    /** No worker waits for the main thread; synchronous listeners are composed into the returned future. */
    public static CompletableFuture<AbstractChatEvent> dispatchAsync(Player player, String message, Component original,
            SignedMessage signed, Executor worker, Executor primary) {
        var state = new State(player, message, original, signed);
        return CompletableFuture.supplyAsync(() -> legacy(state, true), worker)
                .thenCompose(current -> hasListeners(PlayerChatEvent.getHandlerList())
                        ? CompletableFuture.supplyAsync(() -> { legacySync(current); return current; }, primary)
                        : CompletableFuture.completedFuture(current))
                .thenApplyAsync(current -> modern(current, true), worker)
                .thenCompose(current -> hasListeners(ChatEvent.getHandlerList())
                        ? CompletableFuture.supplyAsync(() -> modernSync(current), primary)
                        : CompletableFuture.completedFuture(current));
    }

    /** Composes plugin events inside the existing native per-connection message chain after filtering and decoration finish. */
    public static CompletableFuture<Void> chain(ServerPlayerEntity player, CompletableFuture<Void> ready,
            Consumer<? super Void> nativeContinuation, Executor primary, CompletableFuture<FilteredMessage> filtered,
            CompletableFuture<Text> decorated, net.minecraft.network.message.SignedMessage signed) {
        if (!NativeEntityExtensions.eventsAvailable() || !hasListeners()) return ready.thenAcceptAsync(nativeContinuation, primary);
        return ready.thenComposeAsync(ignored -> {
            // ready is vanilla's allOf(filtered, decorated), so these reads cannot block the server thread.
            var message = signed.withUnsignedContent(decorated.join()).withFilterMask(filtered.join().mask());
            var facade = (Player) NativeEntityExtensions.bukkit(player);
            return dispatchAsync(facade, message.getSignedContent(), NativeChatMessage.component(message.getContent()),
                    NativeChatMessage.adventure(message), Util.getMainWorkerExecutor(), primary)
                    .thenApply(event -> new Prepared(player, message, event));
        }, primary).thenAcceptAsync(prepared -> {
            var previous = PREPARED.get(); PREPARED.set(prepared);
            try { nativeContinuation.accept(null); }
            finally { if (previous == null) PREPARED.remove(); else PREPARED.set(previous); }
        }, primary);
    }

    /** Called only at vanilla's final player-chat broadcast; signature validation, queues and spam checks remain native. */
    public static void broadcast(ServerPlayerEntity player, net.minecraft.network.message.SignedMessage message,
            MessageType.Parameters parameters, Runnable original) {
        var prepared = PREPARED.get();
        if (prepared == null || prepared.player != player || prepared.message.signedBody() != message.signedBody()) {
            original.run(); return;
        }
        var event = prepared.event;
        if (event.isCancelled()) return;
        Player source = event.getPlayer();
        var server = source.getServer();
        Component displayName = source.displayName();
        // The Bukkit default is the profile name; vanilla adds hover/insertion and scoreboard styling.
        // Retain that native presentation unless a plugin supplied a different display name.
        boolean nativeName = displayName.equals(Component.text(source.getName()))
                || displayName.equals(NativeChatMessage.component(parameters.name()));
        var liveViewers = new LinkedHashSet<Audience>(server.getOnlinePlayers()); liveViewers.add(server.getConsoleSender());
        if (event.renderer() instanceof ChatRenderer.Default && event.message().equals(NativeChatMessage.component(message.getContent()))
                && nativeName && event.viewers().equals(liveViewers)) {
            original.run(); return;
        }
        var defaultParameters = nativeName ? parameters
                : new MessageType.Parameters(parameters.type(), NativeChatMessage.text(displayName), parameters.targetName());
        var rawParameters = MessageType.params(NativeChatTypes.RAW, player);
        boolean[] fullyFiltered = {false};
        for (Audience viewer : event.viewers()) deliverNative(viewer, player, message, event, displayName,
                parameters, defaultParameters, rawParameters, fullyFiltered);
        if (fullyFiltered[0]) player.sendMessage(Text.translatable("chat.filtered_full"));
    }

    private static void deliverNative(Audience viewer, ServerPlayerEntity nativeSender, net.minecraft.network.message.SignedMessage message,
            AbstractChatEvent event, Component displayName, MessageType.Parameters originalParameters,
            MessageType.Parameters defaultParameters, MessageType.Parameters rawParameters, boolean[] fullyFiltered) {
        if (viewer instanceof net.kyori.adventure.audience.ForwardingAudience forwarding) {
            for (Audience audience : forwarding.audiences()) deliverNative(audience, nativeSender, message, event, displayName,
                    originalParameters, defaultParameters, rawParameters, fullyFiltered);
            return;
        }
        if (viewer instanceof org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer player) {
            var receiver = player.getHandle();
            boolean filter = nativeSender.shouldFilterMessagesSentTo(receiver);
            fullyFiltered[0] |= filter && message.isFullyFiltered();
            // A non-passthrough mask makes the client display the filtered signed body, not unsigned content.
            // Keep vanilla decoration too, so a custom renderer can never expose filtered text or lose the sender prefix.
            if (filter && !message.filterMask().isPassThrough()) {
                receiver.sendChatMessage(SentMessage.of(message), true, originalParameters); return;
            }
            boolean defaultRenderer = event.renderer() instanceof ChatRenderer.Default;
            Component rendered = defaultRenderer ? event.message()
                    : event.renderer().render(event.getPlayer(), displayName, event.message(), viewer);
            var outgoing = NativeChatMessage.forViewer(message, rendered, filter);
            receiver.sendChatMessage(SentMessage.of(outgoing), filter, defaultRenderer ? defaultParameters : rawParameters);
            return;
        }
        boolean defaultRenderer = event.renderer() instanceof ChatRenderer.Default;
        if (viewer == event.getPlayer().getServer().getConsoleSender()) {
            Component rendered = defaultRenderer ? event.message()
                    : event.renderer().render(event.getPlayer(), displayName, event.message(), viewer);
            nativeSender.getServer().logChatMessage(NativeChatMessage.text(rendered), defaultRenderer ? defaultParameters : rawParameters,
                    message.hasSignature() && !message.isExpiredOnServer(Instant.now()) ? null : "Not Secure");
        } else {
            sendToAudience(viewer, Identity.identity(event.getPlayer().getUniqueId()),
                    event.renderer().render(event.getPlayer(), displayName, event.message(), viewer));
        }
    }

    private static final class State {
        final Component original;
        final SignedMessage signed;
        final String plain;
        Player player;
        String message;
        String format = DEFAULT_LEGACY_FORMAT;
        Set<Player> recipients;
        boolean cancelled;
        State(Player player, String plain, Component original, SignedMessage signed) {
            this.player = player; this.plain = plain; this.message = plain; this.original = original; this.signed = signed;
            recipients = new LinkedHashSet<>(player.getServer().getOnlinePlayers());
        }
    }

    @SuppressWarnings("deprecation")
    private static State legacy(State state, boolean asynchronous) {
        if (!hasListeners(AsyncPlayerChatEvent.getHandlerList()) && !hasListeners(PlayerChatEvent.getHandlerList())) return state;
        var event = new AsyncPlayerChatEvent(asynchronous, state.player, state.message, state.recipients);
        state.player.getServer().getPluginManager().callEvent(event);
        state.message = event.getMessage(); state.format = event.getFormat(); state.recipients = event.getRecipients();
        state.player = event.getPlayer(); state.cancelled = event.isCancelled();
        return state;
    }

    @SuppressWarnings("deprecation")
    private static void legacySync(State state) {
        if (!hasListeners(PlayerChatEvent.getHandlerList())) return;
        var event = new PlayerChatEvent(state.player, state.message, state.format, state.recipients);
        event.setCancelled(state.cancelled); state.player.getServer().getPluginManager().callEvent(event);
        state.message = event.getMessage(); state.format = event.getFormat(); state.recipients = event.getRecipients();
        state.player = event.getPlayer(); state.cancelled = event.isCancelled();
    }

    private static AbstractChatEvent modern(State state, boolean asynchronous) {
        var server = state.player.getServer();
        Component current = state.plain.equals(state.message) ? state.original : LEGACY.deserialize(state.message);
        ChatRenderer renderer = ChatRenderer.defaultRenderer();
        if (!DEFAULT_LEGACY_FORMAT.equals(state.format)) {
            String format = state.format;
            renderer = ChatRenderer.viewerUnaware((source, displayName, content) -> LEGACY.deserialize(
                    String.format(format, source.getDisplayName(), LEGACY.serialize(content))));
        }
        Set<Audience> viewers = new LinkedHashSet<>(state.recipients); viewers.add(server.getConsoleSender());
        var event = new AsyncChatEvent(asynchronous, state.player, viewers, renderer, current, state.original, state.signed);
        event.setCancelled(state.cancelled); server.getPluginManager().callEvent(event);
        return event;
    }

    private static AbstractChatEvent modernSync(AbstractChatEvent event) {
        if (!hasListeners(ChatEvent.getHandlerList())) return event;
        var sync = new ChatEvent(event.getPlayer(), event.viewers(), event.renderer(), event.message(), event.originalMessage(), event.signedMessage());
        sync.setCancelled(event.isCancelled()); event.getPlayer().getServer().getPluginManager().callEvent(sync);
        return sync;
    }

    @SuppressWarnings("deprecation")
    public static boolean hasListeners() {
        return hasListeners(AsyncPlayerChatEvent.getHandlerList()) || hasListeners(PlayerChatEvent.getHandlerList())
                || hasListeners(AsyncChatEvent.getHandlerList()) || hasListeners(ChatEvent.getHandlerList());
    }

    private static void sendToAudience(Audience viewer, Identity sender, Component message) {
        if (viewer instanceof org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer player
                && player.getHandle().getClientChatVisibility() != net.minecraft.client.option.ChatVisibility.FULL) return;
        if (viewer instanceof net.kyori.adventure.audience.ForwardingAudience forwarding) {
            for (Audience audience : forwarding.audiences()) sendToAudience(audience, sender, message);
        } else viewer.sendMessage(sender, message, net.kyori.adventure.audience.MessageType.CHAT);
    }

    private static boolean hasListeners(HandlerList handlers) {
        return handlers.getRegisteredListeners().length != 0;
    }
}
