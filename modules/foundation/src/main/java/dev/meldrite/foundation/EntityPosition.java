package dev.meldrite.foundation;
import java.util.Objects;
/** Immutable semantic position. Live reads and writes remain guarded by the entity/world lease. */
public record EntityPosition(WorldKey world,double x,double y,double z,float yaw,float pitch) {
    public EntityPosition{Objects.requireNonNull(world);if(!Double.isFinite(x)||!Double.isFinite(y)||!Double.isFinite(z)||!Float.isFinite(yaw)||!Float.isFinite(pitch))throw new IllegalArgumentException("Nonfinite entity position");}
}
