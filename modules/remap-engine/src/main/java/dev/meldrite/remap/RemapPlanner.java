package dev.meldrite.remap;

import java.util.Objects;

public final class RemapPlanner {
    public record Plan(String pluginId, String status, boolean transforms, String reason) {}
    private final String targetMinecraft;
    public RemapPlanner(String targetMinecraft) { this.targetMinecraft = Objects.requireNonNull(targetMinecraft); }
    public Plan plan(PluginRequirements requirements) {
        if (!targetMinecraft.equals(requirements.minecraftVersion())) return new Plan(requirements.pluginId(), "REJECTED", false, "WRONG_MINECRAFT_VERSION");
        if (requirements.apiOnly()) return new Plan(requirements.pluginId(), "LINKS_AND_RUNS", false, "API_ONLY");
        if (requirements.namespace() == PluginRequirements.Namespace.UNKNOWN) return new Plan(requirements.pluginId(), "REJECTED", false, "UNKNOWN_NAMESPACE");
        return new Plan(requirements.pluginId(), "LINKS_AND_RUNS", true, "NAMESPACE_REMAP");
    }
}
