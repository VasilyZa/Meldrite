package dev.meldrite.paper.registry;

import java.util.Objects;
import java.util.function.Function;
import org.bukkit.Keyed;
import org.bukkit.Registry;

/** The version binding owns all native registry instances and their projections. */
public final class RegistryProjection {
    private static volatile Function<Class<?>, Registry<?>> provider;
    private RegistryProjection() { }
    public static void bind(Function<Class<?>, Registry<?>> value) { provider = Objects.requireNonNull(value); }
    public static <T extends Keyed> Registry<T> get(Class<T> type) {
        Objects.requireNonNull(type);
        var current = provider;
        if (current == null) throw new IllegalStateException("NATIVE_REGISTRIES_NOT_BOUND");
        @SuppressWarnings("unchecked") Registry<T> registry = (Registry<T>) current.apply(type);
        return registry;
    }
}
