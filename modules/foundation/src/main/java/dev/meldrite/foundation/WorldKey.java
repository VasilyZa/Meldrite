package dev.meldrite.foundation;
import java.util.Objects;
public record WorldKey(ResourceKey key) { public WorldKey { Objects.requireNonNull(key, "key"); } }
