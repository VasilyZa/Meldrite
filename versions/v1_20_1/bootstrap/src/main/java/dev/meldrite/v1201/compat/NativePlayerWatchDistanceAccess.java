package dev.meldrite.v1201.compat;

import net.minecraft.server.network.ServerPlayerEntity;

public interface NativePlayerWatchDistanceAccess {
    void meldrite$refreshWatchDistance(ServerPlayerEntity player, int previousDistance);
    default int meldrite$worldWatchDistance(int current) { return current; }
}
