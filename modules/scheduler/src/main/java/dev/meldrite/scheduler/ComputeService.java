package dev.meldrite.scheduler;

import java.util.Objects;
import java.util.function.BiConsumer;

/** One bounded pool for internal calculations over private snapshots. */
public final class ComputeService {
    private record Work(ParallelRangeExecutor executor, int minimumBatch) {
        Work(WorkerSettings settings) { this(new ParallelRangeExecutor(settings.computeThreads()), settings.computeMinimumBatch()); }
    }
    private static volatile Work work = new Work(WorkerSettings.system());
    private ComputeService() { }
    public static synchronized void configure(WorkerSettings settings) {
        Objects.requireNonNull(settings);
        work.executor.close();
        work = new Work(settings);
    }
    public static void forEach(int length, int minimumBatch, BiConsumer<Integer, Integer> operation) {
        Objects.requireNonNull(operation);
        if (length < 0 || minimumBatch < 1) throw new IllegalArgumentException("Invalid range limits");
        Work current = work;
        var runtime = Runtime.getRuntime();
        long headroom = runtime.maxMemory() - (runtime.totalMemory() - runtime.freeMemory());
        // Do not fan out additional work when the heap is already under pressure.
        // This cannot reclaim the caller's existing snapshot or control other mods.
        long reserve = Math.min(256L * 1024 * 1024, runtime.maxMemory() / 8);
        current.executor.forEach(length, headroom < reserve ? Integer.MAX_VALUE : minimumBatch, operation);
    }
    public static int minimumBatch() { return work.minimumBatch; }
    public static ParallelRangeExecutor.Statistics statistics() { return work.executor.statistics(); }
    public static void close() { work.executor.close(); }
}
