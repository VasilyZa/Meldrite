package dev.meldrite.registry;

import dev.meldrite.foundation.ResourceKey;
import java.util.Objects;

public sealed interface ProjectionResult<T> {
    record Exact<T>(T value) implements ProjectionResult<T> {
        public Exact { Objects.requireNonNull(value); }
    }
    record Opaque<T>(ResourceKey identity, String codecId, byte[] payload) implements ProjectionResult<T> {
        public Opaque { Objects.requireNonNull(identity); Objects.requireNonNull(codecId); payload=Objects.requireNonNull(payload).clone(); }
        @Override public byte[] payload() { return payload.clone(); }
    }
    record Unsupported<T>(String reason) implements ProjectionResult<T> {
        public Unsupported { Objects.requireNonNull(reason); }
    }
    record Unavailable<T>(String reason) implements ProjectionResult<T> {
        public Unavailable { Objects.requireNonNull(reason); }
    }
}
