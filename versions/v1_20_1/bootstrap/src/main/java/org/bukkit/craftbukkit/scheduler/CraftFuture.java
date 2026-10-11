package org.bukkit.craftbukkit.scheduler;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.bukkit.plugin.Plugin;

/** Future task with the same cancellation and completion state transitions as CraftBukkit. */
class CraftFuture<T> extends CraftTask implements Future<T> {
    private final Callable<T> callable;
    private T value;
    private Exception failure;
    CraftFuture(Callable<T> callable, Plugin plugin, int id) { super(plugin, null, id, NO_REPEATING); this.callable = callable; }
    @Override public synchronized boolean cancel(boolean mayInterruptIfRunning) { if (period != NO_REPEATING) return false; period = CANCEL; notifyAll(); return true; }
    @Override public boolean isDone() { return period != NO_REPEATING && period != PROCESS_FOR_FUTURE; }
    @Override public T get() throws InterruptedException, ExecutionException { try { return get(0, TimeUnit.MILLISECONDS); } catch (TimeoutException impossible) { throw new AssertionError(impossible); } }
    @Override public synchronized T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        long remaining = unit.toMillis(timeout), started = System.currentTimeMillis();
        while (!isDone()) { if (remaining == 0) wait(); else { if (remaining <= 0) throw new TimeoutException(); wait(remaining); remaining = unit.toMillis(timeout) - (System.currentTimeMillis() - started); } }
        if (period == CANCEL) throw new CancellationException(); if (failure != null) throw new ExecutionException(failure); return value;
    }
    @Override public void run() { synchronized (this) { if (period == CANCEL) return; period = PROCESS_FOR_FUTURE; } try { value = callable.call(); } catch (Exception error) { failure = error; } finally { synchronized (this) { period = DONE_FOR_FUTURE; notifyAll(); } } }
}
