package dev.meldrite.v1201.compat;

public interface NativePlayerDistanceAccess {
    void meldrite$bindNearbySource(NativeNearbyPlayerSource source);
    void meldrite$refreshPlayerDistances();
    boolean meldrite$weightedViewSources();
    int meldrite$viewSourceLevel(long chunk);
    void meldrite$flushDistanceSources();
    int meldrite$loadingLevel(long chunk);
}
