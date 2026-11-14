package dev.meldrite.scheduler;

import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.BiConsumer;

/** Synchronous pure-array batches: independent ranges, bounded workers and complete publication. */
public final class ParallelRangeExecutor implements AutoCloseable {
    public record Statistics(int parallelism, int poolSize, int active, int queued, int queueCapacity,
                             long completed, long callerFallbacks) { }
    private final int parallelism, queueCapacity;
    private final ThreadPoolExecutor executor;
    // A nested batch can use another executor (including one replaced during reload).
    // Never make a compute worker wait for a second pool's workers.
    private static final ThreadLocal<Boolean> IN_BATCH = new ThreadLocal<>();
    private final LongAdder callerFallbacks = new LongAdder();

    public ParallelRangeExecutor(int parallelism) {
        if (parallelism < 1 || parallelism > 64) throw new IllegalArgumentException("Invalid compute parallelism");
        this.parallelism = parallelism;
        queueCapacity = parallelism * 2;
        int workers = Math.max(1, parallelism - 1);
        var ids = new AtomicInteger();
        executor = new ThreadPoolExecutor(workers, workers, 30, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueCapacity), operation -> {
                    var thread = new Thread(operation, "Meldrite compute worker #" + ids.incrementAndGet());
                    thread.setDaemon(true);
                    thread.setContextClassLoader(ParallelRangeExecutor.class.getClassLoader());
                    return thread;
                }, new ThreadPoolExecutor.AbortPolicy());
        executor.allowCoreThreadTimeOut(true);
    }

    /** The operation may only touch private input snapshots and its exclusive output range. */
    public void forEach(int length, int minimumBatch, BiConsumer<Integer, Integer> operation) {
        java.util.Objects.requireNonNull(operation);
        if (length < 0 || minimumBatch < 1) throw new IllegalArgumentException("Invalid range limits");
        if (length == 0) return;
        int parts = Math.min(parallelism * 4, Math.max(1, length / minimumBatch));
        int lanes = Math.min(parallelism, parts);
        if (lanes == 1 || Boolean.TRUE.equals(IN_BATCH.get()) || executor.isShutdown()) {
            range(operation, 0, length);
            return;
        }
        parallel(length, parts, lanes, operation);
    }

    private void parallel(int length, int parts, int lanes, BiConsumer<Integer, Integer> operation) {
        // Keep at most one queued command per lane. Faster lanes claim more ranges,
        // so skewed spatial queries do not leave all other cores waiting for one range.
        var next = new AtomicInteger();
        Runnable drain = () -> range((ignoredFrom, ignoredTo) -> {
            int part;
            while ((part = next.getAndIncrement()) < parts) {
                int from = (int) ((long) length * part / parts);
                int to = (int) ((long) length * (part + 1) / parts);
                operation.accept(from, to);
            }
        }, 0, length);
        var pending = new ArrayList<FutureTask<Void>>(lanes - 1);
        for (int lane = 1; lane < lanes; lane++) {
            var task = new FutureTask<Void>(drain, null);
            pending.add(task);
            try { executor.execute(task); }
            catch (RejectedExecutionException saturated) {
                // Only pure internal ranges use this fallback, never plugin scheduler callbacks.
                callerFallbacks.increment();
                task.run();
            }
        }
        Throwable failure = null;
        try { drain.run(); }
        catch (Throwable caught) { failure = caught; }
        boolean interrupted = false;
        // Even after failure/interrupt, every writer must finish before this call returns.
        for (var task : pending) {
            // Reclaim any command not picked up yet instead of leaving the caller
            // idle; FutureTask still publishes each lane exactly once.
            if (executor.remove(task)) {
                callerFallbacks.increment();
                task.run();
            }
            boolean done = false;
            while (!done) try { task.get(); done = true; }
            catch (InterruptedException caught) { interrupted = true; }
            catch (ExecutionException caught) {
                if (failure == null) failure = caught.getCause();
                else if (failure != caught.getCause()) failure.addSuppressed(caught.getCause());
                done = true;
            }
        }
        if (interrupted) Thread.currentThread().interrupt();
        if (failure instanceof Error error) throw error;
        if (failure instanceof RuntimeException exception) throw exception;
        if (failure != null) throw new CompletionException(failure);
    }

    private void range(BiConsumer<Integer, Integer> operation, int from, int to) {
        Boolean previous = IN_BATCH.get();
        IN_BATCH.set(true);
        try { operation.accept(from, to); }
        finally { if (previous == null) IN_BATCH.remove(); else IN_BATCH.set(previous); }
    }

    public Statistics statistics() {
        return new Statistics(parallelism, executor.getPoolSize(), executor.getActiveCount(),
                executor.getQueue().size(), queueCapacity, executor.getCompletedTaskCount(), callerFallbacks.sum());
    }

    @Override public void close() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS))
                throw new IllegalStateException("COMPUTE_SHUTDOWN_TIMEOUT");
        } catch (InterruptedException failure) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Compute shutdown interrupted", failure);
        }
    }
}
