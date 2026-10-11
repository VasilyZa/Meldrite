package dev.meldrite.v1201.compat;

/** Paper-compatible state carried by every native explosive projectile. */
public interface ExplosiveProjectileNativeAccess {
    float meldrite$getYield();
    void meldrite$setYield(float value);
    boolean meldrite$isIncendiary();
    void meldrite$setIncendiary(boolean value);
}
