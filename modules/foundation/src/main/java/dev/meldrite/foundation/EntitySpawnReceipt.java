package dev.meldrite.foundation;
import java.util.Objects;
/** Successful native spawn identity; an unavailable type must fail before native mutation. */
public record EntitySpawnReceipt(EntityHandle handle,int nativeId,String name) {
    public EntitySpawnReceipt{Objects.requireNonNull(handle);Objects.requireNonNull(name);if(nativeId<0||name.isBlank())throw new IllegalArgumentException("Invalid spawned entity identity");}
}
