package dev.meldrite.v1201.compat;
import net.minecraft.server.network.ServerPlayerEntity;
public interface NativeEntityTrackerAccess {
    void stopTracking(ServerPlayerEntity viewer);
    void updateTrackedStatus(ServerPlayerEntity viewer);
}
