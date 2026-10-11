package dev.meldrite.v1201.compat;

/** Native Paper-compatible beacon range state exposed to CraftBukkit. */
public interface NativeBeaconRange {
    boolean meldrite$hasEffectRangeOverride();
    double meldrite$getEffectRange();
    void meldrite$setEffectRange(double range);
    void meldrite$resetEffectRange();
}
