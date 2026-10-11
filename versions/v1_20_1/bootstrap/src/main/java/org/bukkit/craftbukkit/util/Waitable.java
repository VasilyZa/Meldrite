package org.bukkit.craftbukkit.util;

import java.util.concurrent.ExecutionException;
import java.util.Objects;

/** A synchronous task with a blocking result, used by command dispatchers. */
public abstract class Waitable<T> implements Runnable {
    private enum Status { WAITING, RUNNING, FINISHED }

    private volatile Status status = Status.WAITING;
    private T value;
    private Throwable failure;

    @Override
    public final void run() {
        synchronized (this) {
            if (status != Status.WAITING) throw new IllegalStateException("Invalid state " + status);
            status = Status.RUNNING;
        }
        try {
            value = evaluate();
        } catch (Throwable throwable) {
            failure = throwable;
        } finally {
            synchronized (this) {
                status = Status.FINISHED;
                notifyAll();
            }
        }
    }

    protected abstract T evaluate();

    public synchronized T get() throws InterruptedException, ExecutionException {
        while (status != Status.FINISHED) wait();
        if (failure != null) throw new ExecutionException(failure);
        return value;
    }
}
