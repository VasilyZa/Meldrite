package dev.meldrite.v1201.compat;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;

/** Permanent removal retires entity tasks; dimension transfers and player respawns retain them. */
public final class NativeEntitySchedulerLifecycle {
    private NativeEntitySchedulerLifecycle() { }

    public static void removed(Entity entity) {
        if (entity instanceof ServerPlayerEntity || !entity.isRemoved()
                || entity.getRemovalReason() == Entity.RemovalReason.CHANGED_DIMENSION) return;
        retireCached(entity);
    }

    public static void loggedOut(ServerPlayerEntity player) {
        if (player.getServer().getPlayerManager().getPlayer(player.getUuid()) != player) retireCached(player);
    }

    /** A failed vanilla portal can remove its source even when it creates no replacement. */
    public static void dimensionTransferCompleted(Entity previous) {
        if (!(previous instanceof ServerPlayerEntity) && previous.isRemoved()
                && previous.getRemovalReason() == Entity.RemovalReason.CHANGED_DIMENSION) {
            // Successful replacement migration clears the source's cache before insertion.
            retireCached(previous);
        }
    }

    private static void retireCached(Entity entity) {
        if (!(entity instanceof PaperEntityAccess access)) return;
        var facade = access.meldrite$getCachedBukkitEntity();
        if (facade != null) facade.meldrite$retireScheduler();
    }
}
