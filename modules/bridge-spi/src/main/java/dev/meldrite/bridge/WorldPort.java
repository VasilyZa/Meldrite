package dev.meldrite.bridge;
import dev.meldrite.foundation.WorldKey;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
public interface WorldPort {
    record WorldInfo(WorldKey key, UUID uid, String name) {
        public WorldInfo { Objects.requireNonNull(key); Objects.requireNonNull(uid); Objects.requireNonNull(name); }
    }
    boolean isLoaded(WorldKey world);
    default int minHeight(WorldKey world) { throw new UnsupportedOperationException("UNSUPPORTED_CAPABILITY: world.min-height"); }
    default int maxHeight(WorldKey world) { throw new UnsupportedOperationException("UNSUPPORTED_CAPABILITY: world.max-height"); }
    default String environment(WorldKey world) { throw new UnsupportedOperationException("UNSUPPORTED_CAPABILITY: world.environment"); }
    default List<WorldInfo> loadedWorlds() { return List.of(); }
    default void save(WorldKey world){throw new UnsupportedOperationException("UNSUPPORTED_CAPABILITY: world.save");}
    default dev.meldrite.foundation.ResourceKey blockType(dev.meldrite.foundation.BlockPosition position) {
        throw new UnsupportedOperationException("UNSUPPORTED_CAPABILITY: world.block-type");
    }
}
