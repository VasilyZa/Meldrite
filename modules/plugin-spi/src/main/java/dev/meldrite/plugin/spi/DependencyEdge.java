package dev.meldrite.plugin.spi;
import java.util.Objects;
import java.util.Optional;
public record DependencyEdge(String target, Phase phase, boolean required, Ordering ordering,
        boolean joinClasspath, String descriptorOrigin, Optional<String> versionRequirement) {
    public enum Phase { BOOTSTRAP, SERVER }
    public enum Ordering { BEFORE_CONSUMER, AFTER_CONSUMER, NONE }
    public DependencyEdge {
        Objects.requireNonNull(target);Objects.requireNonNull(phase);Objects.requireNonNull(ordering);Objects.requireNonNull(descriptorOrigin);Objects.requireNonNull(versionRequirement);
        if(target.isBlank()||descriptorOrigin.isBlank()||versionRequirement.stream().anyMatch(String::isBlank))throw new IllegalArgumentException("Dependency identity/origin/version must not be blank");
    }
}
