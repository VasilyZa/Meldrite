package dev.meldrite.v1201.bridge;

import dev.meldrite.api.ContentAccess;
import dev.meldrite.api.InteropServices;
import dev.meldrite.api.ServerExecutor;

import java.util.Objects;

/** 1.20.1 provider entry point for the neutral extension API. */
public final class InteropProvider {
    private final ContentAccess content;
    private final InteropServices services;
    private final ServerExecutor executor;

    public InteropProvider(ContentAccess content, InteropServices services, ServerExecutor executor) {
        this.content = Objects.requireNonNull(content); this.services = Objects.requireNonNull(services); this.executor = Objects.requireNonNull(executor);
    }

    public ContentAccess content() { return content; }
    public InteropServices services() { return services; }
    public void execute(Runnable task) { executor.execute(Objects.requireNonNull(task)); }
}
