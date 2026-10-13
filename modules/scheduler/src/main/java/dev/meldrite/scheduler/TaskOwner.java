package dev.meldrite.scheduler;
import dev.meldrite.plugin.spi.PluginScope;
import java.util.*;
/** Cancellation is cooperative: closing prevents new work and cancels pending work, never kills a thread. */
public class TaskOwner implements AutoCloseable {
    private final String identity;private final PluginScope scope;private final Set<TaskHandle> tasks=new LinkedHashSet<>();private boolean closed;
    public TaskOwner(String identity,PluginScope scope){this.identity=Objects.requireNonNull(identity);if(identity.isBlank())throw new IllegalArgumentException("Empty task owner");this.scope=Objects.requireNonNull(scope);scope.register(this);}
    public String identity(){return identity;}
    public synchronized boolean active(){return !closed&&!scope.isClosed();}
    synchronized void attach(TaskHandle handle){if(!active())throw new IllegalStateException("OWNER_DISABLED");tasks.add(handle);}
    synchronized void detach(TaskHandle handle){tasks.remove(handle);}
    public synchronized void check(){if(!active())throw new IllegalStateException("OWNER_DISABLED");}
    @Override public void close(){List<TaskHandle> pending;synchronized(this){if(closed)return;closed=true;pending=List.copyOf(tasks);tasks.clear();}for(TaskHandle task:pending)task.cancel();}
}
