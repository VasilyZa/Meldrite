package dev.meldrite.v1201.compat;

/** Immutable snapshot of the model-parts bit mask sent by a Minecraft client. */
public record NativeSkinParts(int getRaw) implements com.destroystokyo.paper.SkinParts {
    @Override public boolean hasCapeEnabled() { return (getRaw & 1) != 0; }
    @Override public boolean hasJacketEnabled() { return (getRaw & 2) != 0; }
    @Override public boolean hasLeftSleeveEnabled() { return (getRaw & 4) != 0; }
    @Override public boolean hasRightSleeveEnabled() { return (getRaw & 8) != 0; }
    @Override public boolean hasLeftPantsEnabled() { return (getRaw & 16) != 0; }
    @Override public boolean hasRightPantsEnabled() { return (getRaw & 32) != 0; }
    @Override public boolean hasHatsEnabled() { return (getRaw & 64) != 0; }
}
