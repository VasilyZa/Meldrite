package dev.meldrite.v1201.compat;
import net.minecraft.server.network.ServerPlayerEntity;
public interface NativeEntityTrackerAccess {
    void meldrite$stopTracking(ServerPlayerEntity viewer);
    void meldrite$updateTrackedStatus(ServerPlayerEntity viewer);
}
