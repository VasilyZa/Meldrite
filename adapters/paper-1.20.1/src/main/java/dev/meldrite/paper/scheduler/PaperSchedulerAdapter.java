package dev.meldrite.paper.scheduler;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import io.papermc.paper.threadedregions.scheduler.*;

/** Paper's region scheduler surface backed by Meldrite's ordinary tick scheduler. */
public final class PaperSchedulerAdapter implements AsyncScheduler, RegionScheduler, GlobalRegionScheduler {
    private final SchedulerAdapter scheduler;
    public PaperSchedulerAdapter(SchedulerAdapter scheduler) { this.scheduler = java.util.Objects.requireNonNull(scheduler); }
    private static long ticks(long amount, TimeUnit unit) { return Math.max(1, (unit.toMillis(Math.max(0, amount)) + 49) / 50); }
    private ScheduledTask schedule(Plugin plugin, Consumer<ScheduledTask> callback, long delay, long period, boolean async) {
        AtomicReference<PaperTask> ref = new AtomicReference<>();
        Runnable action = () -> { PaperTask task = ref.get(); if (task != null) { task.running = true; try { callback.accept(task); } finally { task.running = false; if (!task.repeating) task.finished = true; } } };
        BukkitTask handle = period > 0
                ? (async ? scheduler.runTaskTimerAsynchronously(plugin, action, delay, period) : scheduler.runTaskTimer(plugin, action, delay, period))
                : (async ? scheduler.runTaskLaterAsynchronously(plugin, action, delay) : scheduler.runTaskLater(plugin, action, delay));
        PaperTask task = new PaperTask(plugin, period > 0, handle);
        ref.set(task);
        return task;
    }
    @Override public ScheduledTask runNow(Plugin plugin, Consumer<ScheduledTask> task) { return schedule(plugin, task, 0, -1, true); }
    @Override public ScheduledTask runDelayed(Plugin plugin, Consumer<ScheduledTask> task, long delay, TimeUnit unit) { return schedule(plugin, task, ticks(delay, unit), -1, true); }
    @Override public ScheduledTask runAtFixedRate(Plugin plugin, Consumer<ScheduledTask> task, long initialDelay, long period, TimeUnit unit) { return schedule(plugin, task, ticks(initialDelay, unit), ticks(period, unit), true); }
    @Override public void execute(Plugin plugin, World world, int chunkX, int chunkZ, Runnable task) { scheduler.runTask(plugin, task); }
    @Override public ScheduledTask run(Plugin plugin, World world, int chunkX, int chunkZ, Consumer<ScheduledTask> task) { return schedule(plugin, task, 0, -1, false); }
    @Override public ScheduledTask runDelayed(Plugin plugin, World world, int chunkX, int chunkZ, Consumer<ScheduledTask> task, long delay) { return schedule(plugin, task, delay, -1, false); }
    @Override public ScheduledTask runAtFixedRate(Plugin plugin, World world, int chunkX, int chunkZ, Consumer<ScheduledTask> task, long initialDelay, long period) { return schedule(plugin, task, initialDelay, period, false); }
    @Override public void execute(Plugin plugin, Runnable task) { scheduler.runTask(plugin, task); }
    @Override public ScheduledTask run(Plugin plugin, Consumer<ScheduledTask> task) { return schedule(plugin, task, 0, -1, false); }
    @Override public ScheduledTask runDelayed(Plugin plugin, Consumer<ScheduledTask> task, long delay) { return schedule(plugin, task, delay, -1, false); }
    @Override public ScheduledTask runAtFixedRate(Plugin plugin, Consumer<ScheduledTask> task, long initialDelay, long period) { return schedule(plugin, task, initialDelay, period, false); }
    @Override public void cancelTasks(Plugin plugin) { scheduler.cancelTasks(plugin); }
    private static final class PaperTask implements ScheduledTask {
        final Plugin plugin; final boolean repeating; final BukkitTask delegate;
        volatile boolean running, finished;
        PaperTask(Plugin plugin, boolean repeating, BukkitTask delegate) { this.plugin = plugin; this.repeating = repeating; this.delegate = delegate; }
        @Override public Plugin getOwningPlugin() { return plugin; }
        @Override public boolean isRepeatingTask() { return repeating; }
        @Override public CancelledState cancel() {
            if (delegate.isCancelled()) return CancelledState.CANCELLED_ALREADY;
            if (running) { delegate.cancel(); return CancelledState.RUNNING; }
            delegate.cancel(); return finished ? CancelledState.ALREADY_EXECUTED : CancelledState.CANCELLED_BY_CALLER;
        }
        @Override public ExecutionState getExecutionState() {
            if (delegate.isCancelled()) return running ? ExecutionState.CANCELLED_RUNNING : ExecutionState.CANCELLED;
            if (running) return ExecutionState.RUNNING;
            return finished ? ExecutionState.FINISHED : ExecutionState.IDLE;
        }
    }
}
