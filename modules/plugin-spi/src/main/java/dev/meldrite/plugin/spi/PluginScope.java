package dev.meldrite.plugin.spi;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/** Owns plugin registrations and closes them in reverse registration order. */
public final class PluginScope implements AutoCloseable {
    private final Deque<AutoCloseable> resources = new ArrayDeque<>();
    private boolean closed;

    public <T extends AutoCloseable> T register(T resource) {
        Objects.requireNonNull(resource, "resource");
        synchronized(this){if(!closed){resources.push(resource);return resource;}}
        IllegalStateException rejected=new IllegalStateException("SCOPE_CLOSED");
        try { resource.close(); }
        catch(Throwable failure){if(failure instanceof InterruptedException)Thread.currentThread().interrupt();rejected.addSuppressed(failure);}
        throw rejected;
    }

    public synchronized boolean isClosed() { return closed; }

    @Override public void close() {
        Deque<AutoCloseable> toClose;
        synchronized (this) {
            if (closed) return;
            closed = true;
            toClose = new ArrayDeque<>(resources);
            resources.clear();
        }
        IllegalStateException aggregate = null;
        boolean interrupted = false;
        while (!toClose.isEmpty()) {
            try { toClose.pop().close(); }
            catch (Throwable failure) {
                if (failure instanceof InterruptedException) interrupted = true;
                if (aggregate == null) aggregate = new IllegalStateException("One or more plugin resources failed to close");
                aggregate.addSuppressed(failure);
            }
        }
        if (interrupted) Thread.currentThread().interrupt();
        if (aggregate != null) throw aggregate;
    }
}
