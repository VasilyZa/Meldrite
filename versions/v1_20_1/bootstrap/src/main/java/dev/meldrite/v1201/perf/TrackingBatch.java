package dev.meldrite.v1201.perf;

import dev.meldrite.v1201.compat.NativePlayerDistances;
import dev.meldrite.v1201.hook.PlayerEntityTrackersAccess;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.Vec3d;

/** A tick-scoped broad phase; native tracking and packets remain on the server thread. */
public final class TrackingBatch {
    private static final long MAX_SNAPSHOT_PAIRS = 4_194_304L;
    private static final ThreadLocal<TrackingBatch> CURRENT = new ThreadLocal<>();
    private record Tracked(Entity entity, Vec3d position, Object world, int range, boolean[] nearby) { }
    private final ThreadedAnvilChunkStorage storage;
    private final Map<Object, Tracked> tracked = new IdentityHashMap<>();
    private final Map<ServerPlayerEntity, Integer> players = new IdentityHashMap<>();
    private final Vec3d[] playerPositions;
    private final Object[] playerWorlds;
    private final int[] views;

    private TrackingBatch(ThreadedAnvilChunkStorage storage, List<ServerPlayerEntity> viewers,
                           Object[] sources) {
        this.storage = storage;
        playerPositions = new Vec3d[viewers.size()];
        playerWorlds = new Object[viewers.size()];
        views = new int[viewers.size()];
        double[] playerX = new double[viewers.size()], playerZ = new double[viewers.size()];
        for (int i = 0; i < viewers.size(); i++) {
            ServerPlayerEntity player = viewers.get(i);
            players.put(player, i);
            Vec3d position = playerPositions[i] = player.getPos();
            playerWorlds[i] = player.getWorld();
            playerX[i] = position.x; playerZ[i] = position.z;
            views[i] = NativePlayerDistances.view(player, NativePlayerDistances.worldView(storage));
        }
        double[] x = new double[sources.length], z = new double[sources.length];
        int[] ranges = new int[sources.length];
        Entity[] entities = new Entity[sources.length];
        Vec3d[] positions = new Vec3d[sources.length];
        Object[] worlds = new Object[sources.length];
        for (int i = 0; i < sources.length; i++) {
            TrackingSnapshotSource source = (TrackingSnapshotSource) sources[i];
            Entity entity = entities[i] = source.meldrite$trackedEntity();
            Vec3d position = positions[i] = entity.getPos();
            worlds[i] = entity.getWorld();
            x[i] = position.x; z[i] = position.z; ranges[i] = source.meldrite$trackingRange();
        }
        boolean[][] nearby = DistanceBatch.tracking(x, z, ranges, playerX, playerZ, views);
        for (int i = 0; i < sources.length; i++)
            tracked.put(sources[i], new Tracked(entities[i], positions[i], worlds[i], ranges[i], nearby[i]));
    }

    public static void run(ThreadedAnvilChunkStorage storage, List<ServerPlayerEntity> viewers, Runnable operation) {
        var sources = ((PlayerEntityTrackersAccess) storage).meldrite$entityTrackers();
        // Snapshot/dispatch overhead dominates small rosters; retain their original path.
        long pairs = (long) sources.size() * viewers.size();
        if (!PerformanceSettings.TRACKING || viewers.size() < 16
                || pairs < 16384 || pairs > MAX_SNAPSHOT_PAIRS) {
            operation.run(); return;
        }
        Object[] entries = sources.values().toArray();
        for (Object entry : entries) if (!(entry instanceof TrackingSnapshotSource)) {
            operation.run(); return;
        }
        TrackingBatch previous = CURRENT.get();
        CURRENT.set(new TrackingBatch(storage, viewers, entries));
        try { operation.run(); }
        finally { if (previous == null) CURRENT.remove(); else CURRENT.set(previous); }
    }

    /** Only the already untracked, provably out-of-range no-op can be skipped. */
    public static boolean outside(Object tracker, ServerPlayerEntity player) {
        TrackingBatch batch = CURRENT.get();
        if (batch == null) return false;
        Tracked target = batch.tracked.get(tracker);
        Integer index = batch.players.get(player);
        if (target == null || index == null || target.nearby[index]) return false;
        TrackingSnapshotSource source = (TrackingSnapshotSource) tracker;
        if (source.meldrite$hasTrackingListener(player)) return false;
        Entity entity = source.meldrite$trackedEntity();
        // A callback can teleport, change passengers/range, or change a player's view in this tick.
        // Such changes invalidate the snapshot immediately and use the native method.
        return entity == target.entity && entity != player && entity.getPos() == target.position
                && entity.getWorld() == target.world && player.getPos() == batch.playerPositions[index]
                && player.getWorld() == batch.playerWorlds[index]
                && source.meldrite$trackingRange() == target.range
                && NativePlayerDistances.view(player, NativePlayerDistances.worldView(batch.storage)) == batch.views[index];
    }
}
