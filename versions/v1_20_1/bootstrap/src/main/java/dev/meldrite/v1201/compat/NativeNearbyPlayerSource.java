package dev.meldrite.v1201.compat;

/** Access to the native throttled nearby-player loading tracker. */
public interface NativeNearbyPlayerSource {
    void meldrite$setRadius(int radius);
    void meldrite$source(long chunk, int level);
    void meldrite$flushSources();
    int meldrite$sourceLevel(long chunk);
}
