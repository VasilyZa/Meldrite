package dev.meldrite.v1201.perf;

/** Read once at startup; every optimization retains the native path when disabled. */
public final class PerformanceSettings {
    private static final boolean ENABLED = !Boolean.getBoolean("meldrite.performance.disabled");
    public static final boolean FLUID = enabled("fluid");
    public static final boolean TRACKING = enabled("tracking");
    public static final boolean PATH_INDEX = enabled("path-index");
    public static final boolean SNAPSHOT_COPY = enabled("snapshot-copy");
    public static final boolean COLLISION_CACHE = enabled("collision-cache");
    private PerformanceSettings() { }
    private static boolean enabled(String operation) {
        return ENABLED && !Boolean.getBoolean("meldrite.performance." + operation + ".disabled");
    }
}
