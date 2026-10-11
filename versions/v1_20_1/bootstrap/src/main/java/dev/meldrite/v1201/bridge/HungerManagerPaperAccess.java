package dev.meldrite.v1201.bridge;

/** Paper/CraftBukkit hunger timing values backed by the native manager. */
public interface HungerManagerPaperAccess {
    int meldrite$getSaturatedRegenRate();
    void meldrite$setSaturatedRegenRate(int ticks);
    int meldrite$getUnsaturatedRegenRate();
    void meldrite$setUnsaturatedRegenRate(int ticks);
    int meldrite$getStarvationRate();
    void meldrite$setStarvationRate(int ticks);
}
