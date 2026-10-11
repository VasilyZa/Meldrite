package org.bukkit.craftbukkit.scheduler;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scheduler.BukkitWorker;

/** Compatibility facade over the profile-owned scheduler. */
public class CraftScheduler implements BukkitScheduler {
    private final BukkitScheduler delegate;
    public CraftScheduler() { this(null); }
    public CraftScheduler(BukkitScheduler delegate) { this.delegate = delegate; }
    protected BukkitScheduler scheduler() { BukkitScheduler current = delegate != null ? delegate : Bukkit.getScheduler(); if (current == null || current == this) throw new IllegalStateException("SCHEDULER_UNAVAILABLE"); return current; }
    @Override public int scheduleSyncDelayedTask(Plugin p, Runnable r, long d) { return scheduler().scheduleSyncDelayedTask(p, r, d); }
    @Override public int scheduleSyncDelayedTask(Plugin p, BukkitRunnable r, long d) { return scheduler().scheduleSyncDelayedTask(p, r, d); }
    @Override public int scheduleSyncDelayedTask(Plugin p, Runnable r) { return scheduler().scheduleSyncDelayedTask(p, r); }
    @Override public int scheduleSyncDelayedTask(Plugin p, BukkitRunnable r) { return scheduler().scheduleSyncDelayedTask(p, r); }
    @Override public int scheduleSyncRepeatingTask(Plugin p, Runnable r, long d, long period) { return scheduler().scheduleSyncRepeatingTask(p, r, d, period); }
    @Override public int scheduleSyncRepeatingTask(Plugin p, BukkitRunnable r, long d, long period) { return scheduler().scheduleSyncRepeatingTask(p, r, d, period); }
    @Override public int scheduleAsyncDelayedTask(Plugin p, Runnable r, long d) { return scheduler().scheduleAsyncDelayedTask(p, r, d); }
    @Override public int scheduleAsyncDelayedTask(Plugin p, Runnable r) { return scheduler().scheduleAsyncDelayedTask(p, r); }
    @Override public int scheduleAsyncRepeatingTask(Plugin p, Runnable r, long d, long period) { return scheduler().scheduleAsyncRepeatingTask(p, r, d, period); }
    @Override public <T> Future<T> callSyncMethod(Plugin p, Callable<T> c) { return scheduler().callSyncMethod(p, c); }
    @Override public void cancelTask(int id) { scheduler().cancelTask(id); }
    @Override public void cancelTasks(Plugin p) { scheduler().cancelTasks(p); }
    @Override public boolean isCurrentlyRunning(int id) { return scheduler().isCurrentlyRunning(id); }
    @Override public boolean isQueued(int id) { return scheduler().isQueued(id); }
    @Override public List<BukkitWorker> getActiveWorkers() { return scheduler().getActiveWorkers(); }
    @Override public List<BukkitTask> getPendingTasks() { return scheduler().getPendingTasks(); }
    @Override public BukkitTask runTask(Plugin p, Runnable r) { return scheduler().runTask(p, r); }
    @Override public void runTask(Plugin p, Consumer<BukkitTask> r) { scheduler().runTask(p, r); }
    @Override public BukkitTask runTask(Plugin p, BukkitRunnable r) { return scheduler().runTask(p, r); }
    @Override public BukkitTask runTaskAsynchronously(Plugin p, Runnable r) { return scheduler().runTaskAsynchronously(p, r); }
    @Override public void runTaskAsynchronously(Plugin p, Consumer<BukkitTask> r) { scheduler().runTaskAsynchronously(p, r); }
    @Override public BukkitTask runTaskAsynchronously(Plugin p, BukkitRunnable r) { return scheduler().runTaskAsynchronously(p, r); }
    @Override public BukkitTask runTaskLater(Plugin p, Runnable r, long d) { return scheduler().runTaskLater(p, r, d); }
    @Override public void runTaskLater(Plugin p, Consumer<BukkitTask> r, long d) { scheduler().runTaskLater(p, r, d); }
    @Override public BukkitTask runTaskLater(Plugin p, BukkitRunnable r, long d) { return scheduler().runTaskLater(p, r, d); }
    @Override public BukkitTask runTaskLaterAsynchronously(Plugin p, Runnable r, long d) { return scheduler().runTaskLaterAsynchronously(p, r, d); }
    @Override public void runTaskLaterAsynchronously(Plugin p, Consumer<BukkitTask> r, long d) { scheduler().runTaskLaterAsynchronously(p, r, d); }
    @Override public BukkitTask runTaskLaterAsynchronously(Plugin p, BukkitRunnable r, long d) { return scheduler().runTaskLaterAsynchronously(p, r, d); }
    @Override public BukkitTask runTaskTimer(Plugin p, Runnable r, long d, long period) { return scheduler().runTaskTimer(p, r, d, period); }
    @Override public void runTaskTimer(Plugin p, Consumer<BukkitTask> r, long d, long period) { scheduler().runTaskTimer(p, r, d, period); }
    @Override public BukkitTask runTaskTimer(Plugin p, BukkitRunnable r, long d, long period) { return scheduler().runTaskTimer(p, r, d, period); }
    @Override public BukkitTask runTaskTimerAsynchronously(Plugin p, Runnable r, long d, long period) { return scheduler().runTaskTimerAsynchronously(p, r, d, period); }
    @Override public void runTaskTimerAsynchronously(Plugin p, Consumer<BukkitTask> r, long d, long period) { scheduler().runTaskTimerAsynchronously(p, r, d, period); }
    @Override public BukkitTask runTaskTimerAsynchronously(Plugin p, BukkitRunnable r, long d, long period) { return scheduler().runTaskTimerAsynchronously(p, r, d, period); }
    @Override public Executor getMainThreadExecutor(Plugin p) { return scheduler().getMainThreadExecutor(Objects.requireNonNull(p)); }
}
