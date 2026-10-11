package dev.meldrite.foundation;
import java.util.Objects;
import java.util.Arrays;
public record ItemSnapshot(ResourceKey type, int count, String codecId, byte[] opaquePayload) {
    public ItemSnapshot {
        Objects.requireNonNull(type, "type");
        if (count < 0) throw new IllegalArgumentException("count must be non-negative");
        Objects.requireNonNull(codecId, "codecId");
        if (codecId.isBlank()) throw new IllegalArgumentException("codecId must not be blank");
        opaquePayload = Objects.requireNonNull(opaquePayload, "opaquePayload").clone();
    }
    @Override public byte[] opaquePayload() { return opaquePayload.clone(); }
    @Override public boolean equals(Object value) {
        return value instanceof ItemSnapshot other && type.equals(other.type) && count == other.count
                && codecId.equals(other.codecId) && Arrays.equals(opaquePayload, other.opaquePayload);
    }
    @Override public int hashCode() { return 31 * Objects.hash(type, count, codecId) + Arrays.hashCode(opaquePayload); }
}
