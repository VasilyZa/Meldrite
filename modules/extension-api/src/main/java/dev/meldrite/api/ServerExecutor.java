package dev.meldrite.api;

/** Executes a callback on the host's primary thread. */
@FunctionalInterface
public interface ServerExecutor {
    void execute(Runnable task);
}
