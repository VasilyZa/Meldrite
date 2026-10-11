package dev.meldrite.remap;

import java.util.Map;
import java.util.Objects;

public record NamespaceGraph(Map<PluginRequirements.Namespace, String> namespaces) {
    public NamespaceGraph { namespaces = Map.copyOf(Objects.requireNonNull(namespaces)); }
    public boolean contains(PluginRequirements.Namespace namespace) { return namespaces.containsKey(namespace); }
}
