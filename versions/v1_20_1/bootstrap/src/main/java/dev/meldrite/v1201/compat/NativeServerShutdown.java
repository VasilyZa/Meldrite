package dev.meldrite.v1201.compat;

import io.netty.channel.Channel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Finishes native disconnects while player lifecycle and packet handlers are still available. */
public final class NativeServerShutdown {
    private static final Logger LOGGER = LoggerFactory.getLogger("Meldrite");
    private static final long DISCONNECT_TIMEOUT_NANOS = TimeUnit.SECONDS.toNanos(5);

    private NativeServerShutdown() { }

    public static void disconnectPlayers(MinecraftServer server, Text reason) {
        if (!server.isOnThread()) throw new IllegalStateException("Shutdown disconnects require the server thread");
        Objects.requireNonNull(reason, "reason");
        var manager = server.getPlayerManager();
        if (manager == null) return;

        // A completed disconnect can remove a player immediately, including during this loop.
        var pending = new ArrayList<PendingDisconnect>();
        for (var player : List.copyOf(manager.getPlayerList())) {
            var handler = player.networkHandler;
            var connection = ((NativePlayerConnectionAccess) handler).meldrite$connection();
            var channel = Objects.requireNonNull(((NativeConnectionAccess) connection).meldrite$channel(),
                    "Online player connection has no channel");
            pending.add(new PendingDisconnect(handler, connection, channel));
        }
        for (var disconnect : pending) {
            if (disconnect.channel().isOpen()) disconnect.handler().disconnect(reason);
        }
        // Flush after every disconnect has been queued, including pipelines that coalesce writes.
        for (var disconnect : pending) disconnect.channel().flush();

        long deadline = System.nanoTime() + DISCONNECT_TIMEOUT_NANOS;
        int incomplete = 0;
        for (var disconnect : pending) {
            var channel = disconnect.channel();
            var closed = channel.closeFuture();
            long remaining = deadline - System.nanoTime();
            // Waiting on a channel's own event loop would prevent its write callback from running.
            if (!closed.isDone() && remaining > 0 && !channel.eventLoop().inEventLoop()) {
                closed.awaitUninterruptibly(remaining, TimeUnit.NANOSECONDS);
            }
            if (closed.isDone()) {
                if (!((NativeConnectionAccess) disconnect.connection()).meldrite$disconnectionHandled()) {
                    disconnect.connection().handleDisconnection();
                }
            } else incomplete++;
        }
        if (incomplete != 0) {
            LOGGER.warn("{} player connection(s) did not finish disconnecting before shutdown cleanup", incomplete);
        }
    }

    private record PendingDisconnect(ServerPlayNetworkHandler handler, ClientConnection connection, Channel channel) { }
}
