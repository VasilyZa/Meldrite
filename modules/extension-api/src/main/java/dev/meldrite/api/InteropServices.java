package dev.meldrite.api;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Shared service registry; values cross the boundary as the exact same DTO instance. */
public final class InteropServices {
    private final Map<Class<?>, Object> services = new ConcurrentHashMap<>();

    public <T> void register(Class<T> type, T service) {
        services.putIfAbsent(Objects.requireNonNull(type), Objects.requireNonNull(service));
    }

    public <T> Optional<T> lookup(Class<T> type) {
        Object service = services.get(Objects.requireNonNull(type));
        return service == null ? Optional.empty() : Optional.of(type.cast(service));
    }

    public <T> boolean unregister(Class<T> type, T expected) { return services.remove(type, expected); }
}
