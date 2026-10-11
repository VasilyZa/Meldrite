package dev.meldrite.bootstrap;

import java.util.Objects;
import java.util.Set;

/** Checks observed environment and actual port presence, never inferred support. */
public final class DistributionVerifier {
    public record Manifest(String id, String minecraftVersion, String profile, int java,
                           String namespace, Set<String> capabilities, Set<String> ports) {
        public Manifest {
            token(id); token(minecraftVersion); token(profile); token(namespace);
            if (java < 1) throw new IllegalArgumentException("Invalid Java requirement");
            capabilities = validated(capabilities); ports = validated(ports);
        }
    }
    public record Environment(String minecraftVersion, String profile, int java,
                              String namespace, Set<String> duplicateSharedApi,
                              Set<String> capabilities, Set<String> ports) {
        public Environment {
            token(minecraftVersion); token(profile); token(namespace);
            if (java < 1) throw new IllegalArgumentException("Invalid Java environment");
            duplicateSharedApi = validated(duplicateSharedApi);
            capabilities = validated(capabilities); ports = validated(ports);
        }
    }
    public static final class Rejected extends IllegalStateException {
        private final String code;
        public Rejected(String code, String detail) { super(code + ": " + detail); this.code = code; }
        public String code() { return code; }
    }
    public void verify(Manifest manifest, Environment environment) {
        Objects.requireNonNull(manifest); Objects.requireNonNull(environment);
        if (!manifest.minecraftVersion().equals(environment.minecraftVersion())
                || !manifest.profile().equals(environment.profile())
                || !manifest.namespace().equals(environment.namespace())) {
            throw new Rejected("DISTRIBUTION_MISMATCH", "Version, profile or mapping namespace differs");
        }
        if (environment.java() < manifest.java()) {
            throw new Rejected("JAVA_REQUIREMENT", "Requires Java " + manifest.java() + " or newer");
        }
        if (!environment.duplicateSharedApi().isEmpty()) throw new Rejected("DUPLICATE_SHARED_API", environment.duplicateSharedApi().toString());
        if (!environment.capabilities().containsAll(manifest.capabilities()) || !environment.ports().containsAll(manifest.ports())) {
            throw new Rejected("MISSING_REQUIRED_CAPABILITY", "Required capability or actual port absent");
        }
    }
    private static void token(String value) {
        if (Objects.requireNonNull(value).isBlank()) throw new IllegalArgumentException("Empty manifest value");
    }
    private static Set<String> validated(Set<String> values) {
        var copy = Set.copyOf(values); copy.forEach(DistributionVerifier::token); return copy;
    }
}
