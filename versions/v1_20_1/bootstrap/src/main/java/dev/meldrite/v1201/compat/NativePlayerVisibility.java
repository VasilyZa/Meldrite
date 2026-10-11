package dev.meldrite.v1201.compat;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import dev.meldrite.v1201.hook.PlayerEntityTrackersAccess;

/** Applies visibility to initial tracking, re-tracking, and immediate hide/show. */
public final class NativePlayerVisibility {
    private NativePlayerVisibility() {}
    public static boolean canSee(ServerPlayerEntity viewer, Entity entity) {
        return viewer.getUuid().equals(entity.getUuid()) || ((PaperPlayerAccess) viewer).meldrite$playerState()
                .canSee(entity.getUuid(), ((PaperEntityAccess) entity).meldrite$isVisibleByDefault());
    }
    public static void update(ServerPlayerEntity viewer, Entity entity, boolean visible) {
        if (viewer == entity || viewer.networkHandler == null) return;
        if (entity instanceof ServerPlayerEntity player) {
            viewer.networkHandler.sendPacket(visible
                    ? net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.entryFromPlayer(java.util.List.of(player))
                    : new net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket(java.util.List.of(player.getUuid())));
        }
        if (!(entity.getWorld() instanceof ServerWorld world) || viewer.getServerWorld() != world) return;
        var storage = world.getChunkManager().threadedAnvilChunkStorage;
        var tracker = ((PlayerEntityTrackersAccess) storage).meldrite$entityTrackers().get(entity.getId());
        if (tracker instanceof NativeEntityTrackerAccess access) {
            if (visible) access.updateTrackedStatus(viewer); else access.stopTracking(viewer);
        }
    }
}
