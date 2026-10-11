package dev.meldrite.plugin.spi;
import java.util.*;
public record PluginDescriptor(PluginIdentity identity, Kind kind, String mainClass, String descriptorOrigin,
        Optional<String> bootstrapper, Optional<String> loader, EnablePhase enablePhase,
        List<String> provides, List<DependencyEdge> dependencies, Map<String,String> properties, int discoveryIndex) {
    public enum Kind { LEGACY, PAPER }
    public enum EnablePhase { STARTUP, POSTWORLD }
    public PluginDescriptor {
        Objects.requireNonNull(identity);Objects.requireNonNull(kind);Objects.requireNonNull(mainClass);Objects.requireNonNull(descriptorOrigin);Objects.requireNonNull(bootstrapper);Objects.requireNonNull(loader);Objects.requireNonNull(enablePhase);
        provides=List.copyOf(provides);dependencies=List.copyOf(dependencies);properties=Map.copyOf(properties);
        if(mainClass.isBlank()||descriptorOrigin.isBlank()||discoveryIndex<0)throw new IllegalArgumentException("Invalid plugin descriptor");
    }
}
