package dev.meldrite.bridge;
public interface ServerLifecyclePort {
    AutoCloseable onReady(Runnable listener);
    AutoCloseable onStopping(Runnable listener);
}
