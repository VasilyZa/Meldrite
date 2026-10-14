package dev.meldrite.v1201.perf;

import dev.meldrite.scheduler.ComputeService;

/** Scope parallel array copying to the server's holder-map publication only. */
public final class ChunkHolderSnapshot {
    // Below ~5 MiB of array data, native clone avoids worker dispatch overhead.
    public static final int PARALLEL_THRESHOLD = 262_144;
    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

    private ChunkHolderSnapshot() { }

    public static void enter() { DEPTH.set(DEPTH.get() + 1); }
    public static void exit() {
        int depth = DEPTH.get() - 1;
        if (depth == 0) DEPTH.remove();
        else DEPTH.set(depth);
    }
    public static boolean shouldCopy(int length) {
        return PerformanceSettings.SNAPSHOT_COPY && length >= PARALLEL_THRESHOLD && DEPTH.get() != 0;
    }

    public static long[] copy(long[] source) {
        long[] target = new long[source.length];
        ComputeService.forEach(source.length, 65_536,
                (from, to) -> System.arraycopy(source, from, target, from, to - from));
        return target;
    }

    public static Object[] copy(Object[] source) {
        // Preserve the runtime component type, including mod-supplied array subclasses.
        Object[] target = (Object[]) java.lang.reflect.Array.newInstance(
                source.getClass().getComponentType(), source.length);
        ComputeService.forEach(source.length, 65_536,
                (from, to) -> System.arraycopy(source, from, target, from, to - from));
        return target;
    }
}
