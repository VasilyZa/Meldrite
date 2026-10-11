package dev.meldrite.v1201.runtime;

import net.minecraft.server.MinecraftServer;

/** Holds the actual running server for compatibility entry points injected into MinecraftServer. */
public final class ServerSingletonAccess {
    private static volatile MinecraftServer instance;

    private ServerSingletonAccess() {
    }

    public static void bind(MinecraftServer server) {
        instance = java.util.Objects.requireNonNull(server, "server");
    }

    public static MinecraftServer getServer() {
        MinecraftServer server = instance;
        if (server == null) {
            throw new IllegalStateException("MELDRITE_SERVER_NOT_READY");
        }
        return server;
    }
}
