package dev.meldrite.scheduler;

import java.util.function.BiConsumer;

/** One bounded pool for internal calculations over private snapshots. */
public final class ComputeService {
    private record Work(ParallelRangeExecutor executor, int minimumBatch) {
        Work(WorkerSettings settings) { this(new ParallelRangeExecutor(settings.computeThreads()), settings.computeMinimumBatch()); }
    }
    private static volatile Work work = new Work(WorkerSettings.system());
    private ComputeService() { }
    public static synchronized void configure(WorkerSettings settings) {
        work.executor.close();
        work = new Work(settings);
    }
    public static void forEach(int length, int minimumBatch, BiConsumer<Integer, Integer> operation) {
        work.executor.forEach(length, minimumBatch, operation);
    }
    public static int minimumBatch() { return work.minimumBatch; }
    public static ParallelRangeExecutor.Statistics statistics() { return work.executor.statistics(); }
    public static void close() { work.executor.close(); }
}
