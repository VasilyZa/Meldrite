package dev.meldrite.compat;

import java.util.Map;
import java.util.Objects;

public record CompatibilityContext(String minecraftVersion, String profile, String jarHash, Map<String, Object> attributes) {
    public CompatibilityContext { Objects.requireNonNull(minecraftVersion); Objects.requireNonNull(profile); Objects.requireNonNull(jarHash); attributes = Map.copyOf(attributes); }
}
