package dev.meldrite.foundation;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
public record CapabilitySet(Set<String> names) {
    public CapabilitySet(Collection<String> names) { this(Set.copyOf(names)); }
    public CapabilitySet {
        Objects.requireNonNull(names, "names");
        if (names.stream().anyMatch(name -> name == null || name.isBlank())) throw new IllegalArgumentException("capability names must not be blank");
        names = Set.copyOf(names);
    }
    public boolean supports(String name) { return names.contains(Objects.requireNonNull(name, "name")); }
}
