package dev.meldrite.remap;

import java.util.Objects;

public record PluginRequirements(String pluginId, String minecraftVersion, Namespace namespace, boolean apiOnly, boolean signed) {
    public enum Namespace { SPIGOT, MOJANG, INTERMEDIARY, UNKNOWN }
    public PluginRequirements {
        Objects.requireNonNull(pluginId); Objects.requireNonNull(minecraftVersion); Objects.requireNonNull(namespace);
        if (pluginId.isBlank() || minecraftVersion.isBlank()) throw new IllegalArgumentException("plugin requirements must be named");
    }
}
