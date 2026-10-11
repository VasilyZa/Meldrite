package dev.meldrite.v1201.compat;

/** Null retains the entity's native pickup default; only API writes set an override. */
public interface PaperLivingPickupAccess {
    Boolean meldrite$getPickupOverride();
    void meldrite$setPickupOverride(Boolean pickup);
}
