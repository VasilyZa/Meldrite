package dev.meldrite.scheduler;

import java.util.Objects;
import java.util.Properties;

/** CPU-aware limits for Meldrite-owned background work; game thread ownership is unchanged. */
public record WorkerSettings(int processors, int asyncThreads, int asyncQueueCapacity,
                             int computeThreads, int computeMinimumBatch) {
    public WorkerSettings {
        if (processors < 1 || asyncThreads < 1 || asyncThreads > 64 || asyncQueueCapacity < 1
                || asyncQueueCapacity > 65536 || computeThreads < 1 || computeThreads > 64
                || computeMinimumBatch < 1) throw new IllegalArgumentException("Invalid worker limits");
    }

    public static WorkerSettings system() {
        return resolve(Runtime.getRuntime().availableProcessors(), Runtime.getRuntime().maxMemory(), System.getProperties());
    }

    public static WorkerSettings resolve(int processors, Properties properties) {
        return resolve(processors, Runtime.getRuntime().maxMemory(), properties);
    }

    public static WorkerSettings resolve(int processors, long maximumHeap, Properties properties) {
        Objects.requireNonNull(properties);
        if (processors < 1) throw new IllegalArgumentException("No available processors");
        if (maximumHeap < 1) throw new IllegalArgumentException("No available heap");
        // This is a concurrency budget, not a per-task allocation reservation.
        // Respect the JVM's container-aware heap limit on small servers.
        int memoryLanes = (int) Math.max(1, Math.min(64, maximumHeap / (256L * 1024 * 1024)));
        int async = limit(properties, "meldrite.async.threads", 0, 0, 64);
        if (async == 0) async = Math.min(memoryLanes, Math.min(8, Math.max(1, processors / 2)));
        int compute = limit(properties, "meldrite.compute.threads", 0, 0, 64);
        // Keep room for the game thread and independent Mod pools while allowing
        // larger pure batches to use more cores. The caller is one of these lanes.
        if (compute == 0) compute = Math.min(memoryLanes, Math.min(16, Math.max(1, processors / 2)));
        int queue = limit(properties, "meldrite.async.queue-capacity", 0, 0, 65536);
        if (queue == 0) queue = Math.max(32, async * 8);
        return new WorkerSettings(processors, async, queue,
                compute, limit(properties, "meldrite.compute.minimum-batch", 4096, 1, Integer.MAX_VALUE));
    }

    private static int limit(Properties properties, String key, int fallback, int minimum, int maximum) {
        String value = properties.getProperty(key);
        if (value == null) return fallback;
        try {
            int parsed = Integer.parseInt(value.trim());
            if (parsed >= minimum && parsed <= maximum) return parsed;
        } catch (NumberFormatException ignored) { }
        throw new IllegalArgumentException(key + " must be between " + minimum + " and " + maximum);
    }
}
