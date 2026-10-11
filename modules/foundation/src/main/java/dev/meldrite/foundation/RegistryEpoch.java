package dev.meldrite.foundation;
import java.util.Objects;
public record RegistryEpoch(String scope, long revision) {
    public RegistryEpoch { Objects.requireNonNull(scope, "scope"); if (scope.isBlank() || revision < 0) throw new IllegalArgumentException("Invalid registry epoch"); }
}
