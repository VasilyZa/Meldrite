package dev.meldrite.foundation;
import java.util.Objects;
import java.util.UUID;
public record EntityHandle(WorldKey world, UUID uuid, long generation) {
    public EntityHandle { Objects.requireNonNull(world, "world"); Objects.requireNonNull(uuid, "uuid"); if (generation < 0) throw new IllegalArgumentException("generation must be non-negative"); }
    public boolean isCurrent(long currentGeneration) { return generation == currentGeneration; }
}
