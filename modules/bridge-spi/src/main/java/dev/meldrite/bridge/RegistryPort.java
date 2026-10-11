package dev.meldrite.bridge;
import dev.meldrite.foundation.RegistryEpoch;
import dev.meldrite.foundation.ResourceKey;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
public interface RegistryPort {
    Optional<ResourceKey> resolve(ResourceKey key, RegistryEpoch epoch);
    Set<ResourceKey> snapshot(String scope);
    AutoCloseable lifecycle(Consumer<ResourceKey> listener);
}
