package dev.meldrite.v1201.compat;

import net.kyori.adventure.util.TriState;

public interface PaperItemAccess {
    boolean meldrite$canMobPickup();
    void meldrite$setCanMobPickup(boolean value);
    TriState meldrite$getFrictionState();
    void meldrite$setFrictionState(TriState value);
}
