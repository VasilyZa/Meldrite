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
        return resolve(Runtime.getRuntime().availableProcessors(), System.getProperties());
    }

    public static WorkerSettings resolve(int processors, Properties properties) {
        Objects.requireNonNull(properties);
        if (processors < 1) throw new IllegalArgumentException("No available processors");
        int async = limit(properties, "meldrite.async.threads", 0, 0, 64);
        if (async == 0) async = Math.min(8, Math.max(2, processors / 2));
        int compute = limit(properties, "meldrite.compute.threads", 0, 0, 64);
        if (compute == 0) compute = Math.min(4, Math.max(1, processors / 4));
        return new WorkerSettings(processors, async,
                limit(properties, "meldrite.async.queue-capacity", Math.max(128, async * 32), 1, 65536),
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
