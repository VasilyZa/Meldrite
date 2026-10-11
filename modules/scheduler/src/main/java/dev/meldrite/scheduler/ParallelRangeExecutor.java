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
    private final ThreadLocal<Boolean> inBatch = new ThreadLocal<>();
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
        int parts = Math.min(parallelism, Math.max(1, length / minimumBatch));
        if (parts == 1 || Boolean.TRUE.equals(inBatch.get()) || executor.isShutdown()) {
            range(operation, 0, length);
            return;
        }
        var pending = new ArrayList<FutureTask<Void>>(parts - 1);
        for (int part = 1; part < parts; part++) {
            int from = (int) ((long) length * part / parts);
            int to = (int) ((long) length * (part + 1) / parts);
            var task = new FutureTask<Void>(() -> { range(operation, from, to); return null; });
            pending.add(task);
            try { executor.execute(task); }
            catch (RejectedExecutionException saturated) {
                // Only pure internal ranges use this fallback, never plugin scheduler callbacks.
                callerFallbacks.increment();
                task.run();
            }
        }
        Throwable failure = null;
        try { range(operation, 0, length / parts); }
        catch (Throwable caught) { failure = caught; }
        boolean interrupted = false;
        // Even after failure/interrupt, every writer must finish before this call returns.
        for (var task : pending) {
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
        Boolean previous = inBatch.get();
        inBatch.set(true);
        try { operation.accept(from, to); }
        finally { if (previous == null) inBatch.remove(); else inBatch.set(previous); }
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
