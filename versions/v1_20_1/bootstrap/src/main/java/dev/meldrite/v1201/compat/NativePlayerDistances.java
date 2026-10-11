package dev.meldrite.v1201.compat;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;

/** Effective radii are read by both native loading sources and native player tracking. */
public final class NativePlayerDistances {
    private NativePlayerDistances() {}
    public static int worldView(ThreadedAnvilChunkStorage storage) {
        int current = ((dev.meldrite.v1201.hook.ThreadedAnvilChunkStoragePaperAccessMixin) storage).meldrite$watchDistance();
        return storage instanceof NativePlayerWatchDistanceAccess access ? access.meldrite$worldWatchDistance(current) : current;
    }
    public static int view(ServerPlayerEntity player, int worldDistance) {
        int override = ((PaperPlayerAccess) player).meldrite$playerState().viewDistance;
        return override < 0 ? worldDistance : override;
    }
    public static int simulation(ServerPlayerEntity player, int worldDistance) {
        int override = ((PaperPlayerAccess) player).meldrite$playerState().simulationDistance;
        return override < 0 ? worldDistance : override;
    }
    public static void validate(int distance) {
        if (distance != -1 && (distance < 2 || distance > 32)) throw new IllegalArgumentException("Distance must be -1 or between 2 and 32");
    }
    public static void refreshSources(ThreadedAnvilChunkStorage storage) {
        ((NativePlayerDistanceAccess) storage.getTicketManager()).meldrite$refreshPlayerDistances();
    }
}
