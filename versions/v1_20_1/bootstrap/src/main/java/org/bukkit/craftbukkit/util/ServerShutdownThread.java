package org.bukkit.craftbukkit.util;
import net.minecraft.server.MinecraftServer;
/** Performs the server close operation on a dedicated shutdown thread. */
public final class ServerShutdownThread extends Thread {
    private final MinecraftServer server;
    public ServerShutdownThread(MinecraftServer server) { super("ServerShutdownThread"); this.server = java.util.Objects.requireNonNull(server, "server"); setDaemon(true); }
    @Override public void run() { try { ((AutoCloseable) server).close(); } catch (Exception failure) { Thread current = Thread.currentThread(); current.getUncaughtExceptionHandler().uncaughtException(current, failure); } }
}
