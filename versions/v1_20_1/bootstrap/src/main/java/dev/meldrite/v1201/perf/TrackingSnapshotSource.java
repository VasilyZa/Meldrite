package dev.meldrite.v1201.perf;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;

public interface TrackingSnapshotSource {
    Entity meldrite$trackedEntity();
    int meldrite$trackingRange();
    boolean meldrite$hasTrackingListener(ServerPlayerEntity player);
}
