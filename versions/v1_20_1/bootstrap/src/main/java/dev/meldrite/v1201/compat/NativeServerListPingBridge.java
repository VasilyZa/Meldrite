package dev.meldrite.v1201.compat;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import net.minecraft.server.ServerMetadata;
import org.bukkit.Bukkit;

/** Native status requests dispatch through the ordinary Bukkit plugin manager on the query thread. */
public final class NativeServerListPingBridge {
    private NativeServerListPingBridge() {}

    public static ServerMetadata response(ServerMetadata metadata, SocketAddress remoteAddress) {
        var server = Bukkit.getServer();
        if (server == null) return metadata;
        InetAddress address = remoteAddress instanceof InetSocketAddress socket && socket.getAddress() != null
                ? socket.getAddress() : InetAddress.getLoopbackAddress();
        var event = new NativeServerListPingEvent("", address, metadata, server.getOnlinePlayers());
        server.getPluginManager().callEvent(event);
        return event.response(metadata, server.getHideOnlinePlayers());
    }
}
