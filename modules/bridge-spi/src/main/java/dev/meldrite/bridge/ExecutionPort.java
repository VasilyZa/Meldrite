package dev.meldrite.bridge;
public interface ExecutionPort {
    boolean isPrimaryThread();
    void executePrimary(Runnable task);
    long currentTick();
}
