package dev.meldrite.v1201.compat;

/** Paper block-state cache values consumed by the native light and collision paths. */
public interface NativeBlockStateCacheAccess {
    boolean isConditionallyFullOpaque();
    int getOpacityIfCached();
    boolean shapeExceedsCube();
    long getBlockCollisionBehavior();
}
