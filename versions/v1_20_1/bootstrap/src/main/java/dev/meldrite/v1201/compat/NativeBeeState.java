package dev.meldrite.v1201.compat;

import net.kyori.adventure.util.TriState;

public interface NativeBeeState {
    TriState meldrite$rollingOverride();
    void meldrite$rollingOverride(TriState value);
}
