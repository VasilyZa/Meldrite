package dev.meldrite.v1201.bridge;

import dev.meldrite.bridge.DataFixPort;
import dev.meldrite.foundation.ResourceKey;

import java.util.Objects;

/** Native data-fixer boundary. Identity migrations are safe; other versions require evidence. */
public final class NativeDataFixBridge implements DataFixPort {
    @Override public byte[] update(ResourceKey dataType, int fromVersion, int toVersion, byte[] input) {
        Objects.requireNonNull(dataType, "dataType");
        Objects.requireNonNull(input, "input");
        if (fromVersion != toVersion) throw new UnsupportedOperationException("UNSUPPORTED_NATIVE_DATA_FIX: " + dataType);
        return input.clone();
    }
}
