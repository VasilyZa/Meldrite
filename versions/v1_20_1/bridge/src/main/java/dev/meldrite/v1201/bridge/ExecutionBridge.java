package dev.meldrite.v1201.bridge;

import dev.meldrite.bridge.ExecutionPort;
import net.minecraft.server.MinecraftServer;

import java.util.Objects;

/** Adapts only the server thread and tick clock into the neutral execution port. */
public final class ExecutionBridge implements ExecutionPort {
    private final MinecraftServer server;

    public ExecutionBridge(MinecraftServer server) {
        this.server = Objects.requireNonNull(server, "server");
    }

    @Override
    public boolean isPrimaryThread() {
        return server.isOnThread();
    }

    @Override
    public void executePrimary(Runnable task) {
        Objects.requireNonNull(task, "task");
        server.execute(task);
    }

    @Override
    public long currentTick() {
        return server.getTicks();
    }
}
