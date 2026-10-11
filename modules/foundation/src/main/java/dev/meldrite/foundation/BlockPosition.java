package dev.meldrite.foundation;
import java.util.Objects;
public record BlockPosition(WorldKey world,int x,int y,int z) { public BlockPosition { Objects.requireNonNull(world); } }
