package dev.meldrite.v1201.compat;

import java.util.Objects;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/** One metadata job at a time, no backlog or caller-thread fallback, owned by the server lifecycle. */
public final class NativeAbiAuditExecutor implements AutoCloseable {
    public enum Submission { STARTED, BUSY, CLOSED }
    private final AtomicBoolean running = new AtomicBoolean();
    private final AtomicBoolean closed = new AtomicBoolean();
    private final ThreadPoolExecutor executor;

    public NativeAbiAuditExecutor() {
        executor = new ThreadPoolExecutor(1, 1, 30, TimeUnit.SECONDS, new SynchronousQueue<>(), task -> {
            Thread thread = new Thread(task, "Meldrite-NativeAbiAudit");
            thread.setDaemon(true);
            thread.setContextClassLoader(NativeAbiAuditExecutor.class.getClassLoader());
            return thread;
        }, new ThreadPoolExecutor.AbortPolicy());
        executor.allowCoreThreadTimeOut(true);
    }

    public Submission submit(Runnable job, Consumer<Throwable> failures) {
        Objects.requireNonNull(job); Objects.requireNonNull(failures);
        if (closed.get()) return Submission.CLOSED;
        if (!running.compareAndSet(false, true)) return Submission.BUSY;
        try {
            executor.execute(() -> {
                try { if (!closed.get()) job.run(); }
                catch (Throwable failure) { failures.accept(failure); }
                finally { running.set(false); }
            });
            return Submission.STARTED;
        } catch (RejectedExecutionException rejected) {
            running.set(false);
            return closed.get() ? Submission.CLOSED : Submission.BUSY;
        }
    }

    public boolean isClosed() { return closed.get(); }
    @Override public void close() {
        if (closed.compareAndSet(false, true)) executor.shutdownNow();
    }
}
