package dev.meldrite.v1201.compat;

import com.destroystokyo.paper.network.StatusClient;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import net.minecraft.server.ServerMetadata;
import org.bukkit.Bukkit;

/** Native status requests dispatch through the ordinary Bukkit plugin manager on the query thread. */
public final class NativeServerListPingBridge {
    private NativeServerListPingBridge() {}

    /** The query handler retains the handshake fields for its own connection. */
    public interface HandshakeClient extends StatusClient {
        void meldrite$setHandshake(int protocolVersion, String hostname, int port);
    }

    /** Compatibility entry point when only a remote address, rather than a handshake, is known. */
    public static ServerMetadata response(ServerMetadata metadata, SocketAddress remoteAddress) {
        if (Bukkit.getServer() == null) return metadata;
        if (!(remoteAddress instanceof InetSocketAddress socket)) {
            throw new IllegalArgumentException("Status request address must be an InetSocketAddress: " + remoteAddress);
        }
        return response(metadata, new StatusClient() {
            @Override public InetSocketAddress getAddress() { return socket; }
            @Override public int getProtocolVersion() { return -1; }
            @Override public InetSocketAddress getVirtualHost() { return null; }
        });
    }

    /** Returns null only when the Paper event cancels this request. */
    public static ServerMetadata response(ServerMetadata metadata, StatusClient client) {
        var server = Bukkit.getServer();
        if (server == null) return metadata;
        var event = new NativeServerListPingEvent(client, metadata, server.getOnlinePlayers());
        server.getPluginManager().callEvent(event);
        // The native metadata already applies server-list privacy. Paper permits
        // plugins to replace that initial sample with explicit display entries.
        return event.response(metadata, false);
    }
}
