package dev.meldrite.v1201.bridge;

import dev.meldrite.bridge.ServerLifecyclePort;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.Objects;

/** Owns lifecycle listeners for one Minecraft server instance. */
public final class LifecycleBridge implements ServerLifecyclePort, AutoCloseable {
    private final MinecraftServer server;
    private final ArrayList<Runnable> readyListeners = new ArrayList<>();
    private final ArrayList<Runnable> stoppingListeners = new ArrayList<>();
    private boolean ready;
    private boolean stopping;

    public LifecycleBridge(MinecraftServer server) {
        this.server = Objects.requireNonNull(server, "server");
    }

    @Override
    public synchronized AutoCloseable onReady(Runnable listener) {
        Objects.requireNonNull(listener, "listener");
        if (ready) throw new IllegalStateException("Server is already ready");
        if (stopping) throw new IllegalStateException("Server is stopping");
        readyListeners.add(listener);
        return () -> remove(readyListeners, listener);
    }

    @Override
    public synchronized AutoCloseable onStopping(Runnable listener) {
        Objects.requireNonNull(listener, "listener");
        if (stopping) throw new IllegalStateException("Server is already stopping");
        stoppingListeners.add(listener);
        return () -> remove(stoppingListeners, listener);
    }

    public void signalReady() {
        ArrayList<Runnable> listeners;
        synchronized (this) {
            if (ready || stopping) return;
            ready = true;
            listeners = new ArrayList<>(readyListeners);
        }
        listeners.forEach(Runnable::run);
    }

    public void signalStopping() {
        ArrayList<Runnable> listeners;
        synchronized (this) {
            if (stopping) return;
            stopping = true;
            listeners = new ArrayList<>(stoppingListeners);
        }
        listeners.forEach(Runnable::run);
    }

    public MinecraftServer server() {
        return server;
    }

    @Override
    public void close() {
        signalStopping();
        synchronized (this) {
            readyListeners.clear();
            stoppingListeners.clear();
        }
    }

    private synchronized void remove(ArrayList<Runnable> listeners, Runnable listener) {
        listeners.remove(listener);
    }
}
