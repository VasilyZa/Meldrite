package dev.meldrite.bootstrap;

import dev.meldrite.bridge.ExecutionPort;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.function.Consumer;

/** Orders the shared startup protocol; only the composition root supplies native facts. */
public final class BootstrapCoordinator implements AutoCloseable {
    public enum Phase {
        NEW, DISTRIBUTION_VERIFIED, TRANSFORMS_REGISTERED, SERVICES_BOUND,
        PLUGINS_DISCOVERED, PLUGIN_BOOTSTRAP, PLUGINS_LOADED, STARTUP_ENABLED,
        WORLDS_READY, READY, STOPPING, STOPPED, FAILED
    }

    private final ExecutionPort execution;
    private final Consumer<Phase> observer;
    private final ArrayDeque<AutoCloseable> resources = new ArrayDeque<>();
    private volatile Phase phase = Phase.NEW;
    private boolean closed;

    public BootstrapCoordinator(ExecutionPort execution, Consumer<Phase> observer) {
        this.execution = Objects.requireNonNull(execution);
        this.observer = Objects.requireNonNull(observer);
    }

    public Phase phase() { return phase; }
    public void distributionVerified() { advance(Phase.NEW, Phase.DISTRIBUTION_VERIFIED); }
    public void transformsRegistered() { advance(Phase.DISTRIBUTION_VERIFIED, Phase.TRANSFORMS_REGISTERED); }
    public void servicesBound() { advance(Phase.TRANSFORMS_REGISTERED, Phase.SERVICES_BOUND); }
    public void pluginsDiscovered() { advance(Phase.SERVICES_BOUND, Phase.PLUGINS_DISCOVERED); }
    public void pluginBootstrap() { advance(Phase.PLUGINS_DISCOVERED, Phase.PLUGIN_BOOTSTRAP); }
    public void pluginsLoaded() { advance(Phase.PLUGIN_BOOTSTRAP, Phase.PLUGINS_LOADED); }
    public void startupEnabled() { advance(Phase.PLUGINS_LOADED, Phase.STARTUP_ENABLED); }
    public void worldsReady() { advance(Phase.STARTUP_ENABLED, Phase.WORLDS_READY); }
    public void ready() { advance(Phase.WORLDS_READY, Phase.READY); }

    public <T extends AutoCloseable> T own(T resource) {
        primary();
        if (closed || phase == Phase.FAILED) throw new IllegalStateException("Bootstrap no longer owns resources");
        resources.push(Objects.requireNonNull(resource));
        return resource;
    }

    private void advance(Phase expected, Phase next) {
        primary();
        if (closed || phase != expected) throw new IllegalStateException("Expected " + expected + ", found " + phase);
        phase = next;
        try { observer.accept(next); }
        catch (RuntimeException | Error failure) { phase = Phase.FAILED; throw failure; }
    }

    private void primary() {
        if (!execution.isPrimaryThread()) throw new IllegalStateException("Bootstrap requires the server thread");
    }

    @Override public void close() {
        primary();
        if (closed) return;
        closed = true;
        boolean failedBeforeStop = phase == Phase.FAILED;
        var failure = new IllegalStateException("Bootstrap cleanup failed");
        phase = Phase.STOPPING;
        try { observer.accept(phase); } catch (Throwable cause) { failure.addSuppressed(cause); }
        while (!resources.isEmpty()) {
            try { resources.pop().close(); } catch (Throwable cause) { failure.addSuppressed(cause); }
        }
        phase = failedBeforeStop || failure.getSuppressed().length != 0 ? Phase.FAILED : Phase.STOPPED;
        try { observer.accept(phase); }
        catch (Throwable cause) { phase = Phase.FAILED; failure.addSuppressed(cause); }
        if (failure.getSuppressed().length != 0) throw failure;
    }
}
