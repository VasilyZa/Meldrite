package dev.meldrite.v1201.compat;

import java.util.Objects;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/** Bridges session events while retaining native notification text when listeners leave it unchanged. */
public final class NativePlayerSessionEvents {
    private static final class Join {
        Notification notification;
    }
    private static final class Quit {
        final ServerPlayerEntity player;
        boolean dispatched;
        Quit(ServerPlayerEntity player) { this.player = player; }
    }
    private record Notification(Text text, Consumer<Text> send) {
        Component component() { return text == null ? null : GsonComponentSerializer.gson().deserialize(Text.Serializer.toJson(text)); }
        void deliver(Component message) {
            if (message == null || message.equals(Component.empty())) return;
            Text output = Objects.equals(component(), message) ? text : Text.Serializer.fromJson(GsonComponentSerializer.gson().serialize(message));
            if (output != null && !output.getString().isEmpty()) send.accept(output);
        }
    }
    private static final ThreadLocal<Join> JOIN = new ThreadLocal<>();
    private static final ThreadLocal<Quit> QUIT = new ThreadLocal<>();
    private NativePlayerSessionEvents() { }

    public static void connect(ServerPlayerEntity player, Runnable original) {
        if (!NativeEntityExtensions.eventsAvailable()) { original.run(); return; }
        var previous = JOIN.get(); var join = new Join(); JOIN.set(join);
        try { original.run(); }
        finally { if (previous == null) JOIN.remove(); else JOIN.set(previous); }
        // Listeners need both the authoritative online roster and the usable play connection.
        if (!online(player) || player.networkHandler == null || !player.networkHandler.isConnectionOpen()) return;
        Player facade = (Player) NativeEntityExtensions.bukkit(player);
        var event = new PlayerJoinEvent(facade, join.notification == null ? null : join.notification.component());
        facade.getServer().getPluginManager().callEvent(event);
        if (join.notification != null) join.notification.deliver(event.joinMessage());
        else notification(player, null).deliver(event.joinMessage());
    }

    public static void disconnect(ServerPlayerEntity player, Runnable original) {
        if (!NativeEntityExtensions.eventsAvailable() || !online(player)) { original.run(); return; }
        var previous = QUIT.get(); QUIT.set(new Quit(player));
        try { original.run(); }
        finally { if (previous == null) QUIT.remove(); else QUIT.set(previous); }
    }

    public static void remove(ServerPlayerEntity player, Runnable original) {
        if (!NativeEntityExtensions.eventsAvailable() || !online(player)) {
            original.run();
            NativeEntitySchedulerLifecycle.loggedOut(player);
            return;
        }
        var previous = QUIT.get();
        var quit = previous != null && previous.player == player ? previous : new Quit(player);
        QUIT.set(quit);
        try {
            dispatchQuit(quit, notification(player, null));
            original.run();
            NativeEntitySchedulerLifecycle.loggedOut(player);
        } finally {
            if (previous == null) QUIT.remove(); else QUIT.set(previous);
        }
    }

    /** Observes only the session's join/leave notification; other broadcasts run immediately. */
    public static void broadcast(Text message, boolean overlay, Consumer<Text> original) {
        if (!overlay && message.getContent() instanceof TranslatableTextContent translated) {
            var join = JOIN.get();
            if (join != null && join.notification == null && (translated.getKey().equals("multiplayer.player.joined")
                    || translated.getKey().equals("multiplayer.player.joined.renamed"))) {
                join.notification = new Notification(message, original); return;
            }
            var quit = QUIT.get();
            if (quit != null && !quit.dispatched && translated.getKey().equals("multiplayer.player.left")) {
                dispatchQuit(quit, new Notification(message, original)); return;
            }
        }
        original.accept(message);
    }

    private static void dispatchQuit(Quit quit, Notification notification) {
        if (quit.dispatched) return;
        quit.dispatched = true;
        Player facade = (Player) NativeEntityExtensions.bukkit(quit.player);
        var state = ((PaperPlayerAccess) quit.player).meldrite$playerState();
        var event = new PlayerQuitEvent(facade, state.kickLeaveMessageSet ? state.kickLeaveMessage : notification.component());
        facade.getServer().getPluginManager().callEvent(event);
        notification.deliver(event.quitMessage());
    }
    private static Notification notification(ServerPlayerEntity player, Text message) {
        return new Notification(message, text -> player.getServer().getPlayerManager().broadcast(text, false));
    }
    private static boolean online(ServerPlayerEntity player) {
        return player.getServer().getPlayerManager().getPlayer(player.getUuid()) == player;
    }
}
