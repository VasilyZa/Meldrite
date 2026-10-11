package org.bukkit.craftbukkit.scheduler;

import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/** Task ABI wrapper around the profile's live scheduler handle. */
public class CraftTask implements BukkitTask, Runnable {
    public static final int ERROR = 0, NO_REPEATING = -1, CANCEL = -2, PROCESS_FOR_FUTURE = -3, DONE_FOR_FUTURE = -4;
    protected final Plugin plugin;
    protected final int id;
    protected final Runnable runnable;
    protected final Consumer<BukkitTask> consumer;
    protected final BukkitTask delegate;
    protected volatile long period;
    CraftTask() { this(null, null, null, -1, NO_REPEATING); }
    CraftTask(Object task, int id, String ignoredName) { this(null, task instanceof Runnable ? (Runnable) task : null, task instanceof Consumer<?> ? (Consumer<BukkitTask>) task : null, id, NO_REPEATING); }
    CraftTask(Plugin plugin, Object task, int id, long period) { this(plugin, task instanceof Runnable ? (Runnable) task : null, task instanceof Consumer<?> ? (Consumer<BukkitTask>) task : null, id, period); }
    public CraftTask(BukkitTask delegate) { this(delegate == null ? null : delegate.getOwner(), null, null, delegate == null ? -1 : delegate.getTaskId(), delegate == null ? CANCEL : NO_REPEATING, delegate); }
    private CraftTask(Plugin plugin, Runnable runnable, Consumer<BukkitTask> consumer, int id, long period) { this(plugin, runnable, consumer, id, period, null); }
    private CraftTask(Plugin plugin, Runnable runnable, Consumer<BukkitTask> consumer, int id, long period, BukkitTask delegate) { this.plugin = plugin; this.runnable = runnable; this.consumer = consumer; this.id = id; this.period = period; this.delegate = delegate; }
    @Override public int getTaskId() { return delegate == null ? id : delegate.getTaskId(); }
    @Override public Plugin getOwner() { return delegate == null ? plugin : delegate.getOwner(); }
    @Override public boolean isSync() { return delegate == null || delegate.isSync(); }
    @Override public boolean isCancelled() { return delegate == null ? period == CANCEL : delegate.isCancelled(); }
    @Override public void cancel() { if (delegate != null) delegate.cancel(); else if (Bukkit.getScheduler() != null) Bukkit.getScheduler().cancelTask(id); period = CANCEL; }
    @Override public void run() { if (runnable != null) runnable.run(); else if (consumer != null) consumer.accept(this); }
    public Class<?> getTaskClass() { return runnable == null ? consumer == null ? null : consumer.getClass() : runnable.getClass(); }
    boolean cancel0() { period = CANCEL; cancel(); return true; }
}
