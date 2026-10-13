package dev.meldrite.paper.scheduler;

import dev.meldrite.bridge.ExecutionPort;
import dev.meldrite.plugin.classloading.PluginLoaderEngine;
import dev.meldrite.plugin.classloading.PluginRealm;
import dev.meldrite.plugin.spi.PluginScope;
import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.*;
import java.util.function.*;
import java.util.logging.Level;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.Plugin;

/** Entity clocks on the vanilla server thread, with plugin scope and realm ownership. */
public final class EntitySchedulerService implements AutoCloseable {
    private final ExecutionPort execution;
    private final Function<Plugin, PluginScope> scopes;
    private final PluginLoaderEngine engine;
    private final Set<BoundScheduler> active = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<Plugin, Owner> owners = new IdentityHashMap<>();
    private boolean closed, draining;
    private long sequence;

    public EntitySchedulerService(ExecutionPort execution, Function<Plugin, PluginScope> scopes, PluginLoaderEngine engine) {
        this.execution = Objects.requireNonNull(execution);
        this.scopes = Objects.requireNonNull(scopes);
        this.engine = Objects.requireNonNull(engine);
    }

    /** The presence check runs only on the primary thread and follows the published facade's native replacement. */
    public EntityScheduler create(BooleanSupplier present, boolean retired) {
        return new BoundScheduler(Objects.requireNonNull(present), retired);
    }

    public void retire(EntityScheduler scheduler) {
        requirePrimary();
        if (!(scheduler instanceof BoundScheduler bound) || bound.service() != this) throw new IllegalArgumentException("FOREIGN_ENTITY_SCHEDULER");
        List<Task> notify = new ArrayList<>();
        synchronized (this) {
            if (bound.retired) return;
            bound.retired = true;
            active.remove(bound);
            for (Task task : List.copyOf(bound.tasks)) {
                if (task.state == ScheduledTask.ExecutionState.IDLE) {
                    bound.queue.remove(task);
                    notify.add(task);
                } else if (task.state == ScheduledTask.ExecutionState.RUNNING && task.isRepeatingTask()) {
                    task.state = ScheduledTask.ExecutionState.CANCELLED_RUNNING;
                }
            }
        }
        // Retirement can happen from inside a callback. No plugin code runs under the scheduler lock.
        for (Task task : notify) {
            synchronized (this) {
                if (task.state != ScheduledTask.ExecutionState.IDLE) continue;
                task.state = ScheduledTask.ExecutionState.RUNNING;
            }
            invoke(task, true);
        }
    }

    public void tick() {
        requirePrimary();
        List<BoundScheduler> snapshot;
        synchronized (this) {
            if (closed) return;
            if (draining) throw new IllegalStateException("REENTRANT_ENTITY_SCHEDULER_TICK");
            draining = true;
            snapshot = List.copyOf(active);
        }
        try {
            List<Task> due = new ArrayList<>();
            // Advance all entity clocks before callbacks, so cross-entity submissions run no earlier than next tick.
            for (BoundScheduler bound : snapshot) {
                if (!bound.present.getAsBoolean()) continue;
                synchronized (this) {
                    if (bound.retired || !active.contains(bound)) continue;
                    bound.clock = Math.incrementExact(bound.clock);
                    while (!bound.queue.isEmpty() && bound.queue.peek().due <= bound.clock) due.add(bound.queue.remove());
                }
            }
            for (Task task : due) {
                synchronized (this) {
                    if (task.state != ScheduledTask.ExecutionState.IDLE) continue;
                }
                if (!task.scheduler.present.getAsBoolean()) {
                    synchronized (this) {
                        if (task.state == ScheduledTask.ExecutionState.IDLE) task.scheduler.queue.add(task);
                    }
                    continue;
                }
                synchronized (this) {
                    if (task.state != ScheduledTask.ExecutionState.IDLE) continue;
                    task.state = ScheduledTask.ExecutionState.RUNNING;
                }
                invoke(task, false);
            }
        } finally { synchronized (this) { draining = false; } }
    }

    private void invoke(Task task, boolean retiring) {
        try {
            if (!task.owner.closed && task.owner.plugin.isEnabled() && !task.owner.scope.isClosed()) {
                engine.withContext(task.owner.realm, () -> {
                    if (!task.owner.closed && task.owner.plugin.isEnabled() && !task.owner.scope.isClosed()) {
                        if (retiring) { if (task.retired != null) task.retired.run(); }
                        else task.run.accept(task);
                    }
                    return null;
                });
            }
        } catch (Throwable failure) {
            if (failure instanceof InterruptedException) Thread.currentThread().interrupt();
            task.owner.plugin.getLogger().log(Level.WARNING, "Entity task for " + task.owner.plugin.getName() + " failed", failure);
        } finally {
            synchronized (this) {
                if (retiring) task.state = ScheduledTask.ExecutionState.CANCELLED;
                else if (!task.isRepeatingTask()) task.state = ScheduledTask.ExecutionState.FINISHED;
                else if (task.state == ScheduledTask.ExecutionState.CANCELLED_RUNNING || task.scheduler.retired
                        || closed || task.owner.closed || !task.owner.plugin.isEnabled() || task.owner.scope.isClosed()) {
                    task.state = ScheduledTask.ExecutionState.CANCELLED;
                } else {
                    task.state = ScheduledTask.ExecutionState.IDLE;
                    task.due = deadline(task.scheduler.clock, task.period);
                    task.scheduler.queue.add(task);
                }
                if (task.state != ScheduledTask.ExecutionState.IDLE) detach(task);
            }
        }
    }

    private void detach(Task task) {
        task.scheduler.queue.remove(task);
        task.scheduler.tasks.remove(task);
        task.owner.tasks.remove(task);
        task.run = null; task.retired = null;
        if (task.scheduler.tasks.isEmpty()) active.remove(task.scheduler);
    }

    private Owner owner(Plugin plugin) {
        if (!plugin.isEnabled()) throw new IllegalPluginAccessException("Plugin is not enabled: " + plugin.getName());
        Owner existing = owners.get(plugin);
        if (existing != null) return existing;
        if (!(plugin.getClass().getClassLoader() instanceof PluginRealm realm)) throw new IllegalArgumentException("UNSUPPORTED_PLUGIN_REALM");
        PluginScope scope = Objects.requireNonNull(scopes.apply(plugin), "Unknown plugin scope");
        Owner owner = new Owner(plugin, realm, scope);
        owners.put(plugin, owner);
        try { scope.register(owner); }
        catch (RuntimeException failure) { owners.remove(plugin, owner); throw failure; }
        return owner;
    }

    private final class Owner implements AutoCloseable {
        final Plugin plugin; final PluginRealm realm; final PluginScope scope;
        final Set<Task> tasks = new LinkedHashSet<>();
        volatile boolean closed;
        Owner(Plugin plugin, PluginRealm realm, PluginScope scope) { this.plugin = plugin; this.realm = realm; this.scope = scope; }
        @Override public void close() {
            synchronized (EntitySchedulerService.this) {
                if (closed) return;
                closed = true;
                for (Task task : List.copyOf(tasks)) task.cancel();
                owners.remove(plugin, this);
            }
        }
    }

    private final class BoundScheduler implements EntityScheduler {
        final BooleanSupplier present;
        final Set<Task> tasks = new LinkedHashSet<>();
        final PriorityQueue<Task> queue = new PriorityQueue<>(Comparator.comparingLong((Task task) -> task.due).thenComparingLong(task -> task.order));
        long clock; boolean retired;
        BoundScheduler(BooleanSupplier present, boolean retired) { this.present = present; this.retired = retired; }
        EntitySchedulerService service() { return EntitySchedulerService.this; }
        @Override public boolean execute(Plugin plugin, Runnable run, Runnable retired, long delay) {
            Objects.requireNonNull(run, "run");
            return schedule(plugin, task -> run.run(), retired, Math.max(1, delay), -1) != null;
        }
        @Override public ScheduledTask run(Plugin plugin, Consumer<ScheduledTask> task, Runnable retired) {
            return schedule(plugin, task, retired, 1, -1);
        }
        @Override public ScheduledTask runDelayed(Plugin plugin, Consumer<ScheduledTask> task, Runnable retired, long delay) {
            if (delay < 1) throw new IllegalArgumentException("Delay ticks must be positive");
            return schedule(plugin, task, retired, delay, -1);
        }
        @Override public ScheduledTask runAtFixedRate(Plugin plugin, Consumer<ScheduledTask> task, Runnable retired, long delay, long period) {
            if (delay < 1 || period < 1) throw new IllegalArgumentException("Delay and period ticks must be positive");
            return schedule(plugin, task, retired, delay, period);
        }
        private ScheduledTask schedule(Plugin plugin, Consumer<ScheduledTask> run, Runnable retiredCallback, long delay, long period) {
            Objects.requireNonNull(plugin, "plugin"); Objects.requireNonNull(run, "task");
            synchronized (EntitySchedulerService.this) {
                if (!plugin.isEnabled()) throw new IllegalPluginAccessException("Plugin is not enabled: " + plugin.getName());
                if (retired || closed) return null;
                Owner owner = owner(plugin);
                Task task = new Task(this, owner, run, retiredCallback, deadline(clock, delay), period, ++sequence);
                tasks.add(task); queue.add(task); owner.tasks.add(task); active.add(this);
                if (owner.closed || owner.scope.isClosed() || !plugin.isEnabled()) task.cancel();
                return task;
            }
        }
    }

    private final class Task implements ScheduledTask {
        final BoundScheduler scheduler; final Owner owner; final long period, order;
        Consumer<ScheduledTask> run; Runnable retired; long due;
        ExecutionState state = ExecutionState.IDLE;
        Task(BoundScheduler scheduler, Owner owner, Consumer<ScheduledTask> run, Runnable retired, long due, long period, long order) {
            this.scheduler = scheduler; this.owner = owner; this.run = run; this.retired = retired;
            this.due = due; this.period = period; this.order = order;
        }
        @Override public Plugin getOwningPlugin() { return owner.plugin; }
        @Override public boolean isRepeatingTask() { return period > 0; }
        @Override public ExecutionState getExecutionState() { synchronized (EntitySchedulerService.this) { return state; } }
        @Override public CancelledState cancel() {
            synchronized (EntitySchedulerService.this) {
                return switch (state) {
                    case IDLE -> { state = ExecutionState.CANCELLED; detach(this); yield CancelledState.CANCELLED_BY_CALLER; }
                    case RUNNING -> {
                        if (!isRepeatingTask()) yield CancelledState.RUNNING;
                        state = ExecutionState.CANCELLED_RUNNING;
                        yield CancelledState.NEXT_RUNS_CANCELLED;
                    }
                    case CANCELLED_RUNNING -> CancelledState.NEXT_RUNS_CANCELLED_ALREADY;
                    case FINISHED -> CancelledState.ALREADY_EXECUTED;
                    case CANCELLED -> CancelledState.CANCELLED_ALREADY;
                };
            }
        }
    }

    // Positive API delays have no upper bound. Saturate deadlines instead of wrapping early or aborting a tick.
    private static long deadline(long clock, long delay) { return delay > Long.MAX_VALUE - clock ? Long.MAX_VALUE : clock + delay; }
    private void requirePrimary() { if (!execution.isPrimaryThread()) throw new IllegalStateException("ASYNC_ENTITY_SCHEDULER_LIFECYCLE"); }
    @Override public void close() {
        requirePrimary();
        synchronized (this) {
            if (closed) return;
            closed = true;
            for (Owner owner : List.copyOf(owners.values())) owner.close();
            active.clear();
        }
    }
}
