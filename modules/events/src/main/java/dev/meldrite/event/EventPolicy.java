package dev.meldrite.event;
import dev.meldrite.bridge.ExecutionPort;
import java.util.Objects;
public final class EventPolicy {
    private final ExecutionPort execution;private final int maximumDepth;
    public EventPolicy(ExecutionPort execution,int maximumDepth){this.execution=Objects.requireNonNull(execution);if(maximumDepth<1)throw new IllegalArgumentException("Invalid event depth");this.maximumDepth=maximumDepth;}
    public void check(boolean asynchronous){if(asynchronous&&execution.isPrimaryThread())throw new IllegalStateException("ASYNC_EVENT_ON_PRIMARY");if(!asynchronous&&!execution.isPrimaryThread())throw new IllegalStateException("SYNC_EVENT_OFF_PRIMARY");}
    public int maximumDepth(){return maximumDepth;}
}
