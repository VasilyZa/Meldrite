package dev.meldrite.v1201.perf;

import dev.meldrite.scheduler.ComputeService;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.TargetPathNode;
import net.minecraft.util.math.BlockPos;

/** Exact multi-target heuristic: snapshot geometry is indexed, live node evaluation stays on the caller. */
public final class PathSearchBatch {
    private static final ThreadLocal<PathSearchBatch> CURRENT = new ThreadLocal<>();
    private final Object owner;
    private final TargetPathNode[] targets;
    private final FloatSpatialIndex targetIndex;
    private final ArrayList<PathNode> evaluated = new ArrayList<>();
    private boolean enabled = true, finished;
    private PathSearchBatch(Object owner, Set<TargetPathNode> targets) {
        this.owner = owner;
        this.targets = targets.toArray(TargetPathNode[]::new);
        int[] x = new int[targets.size()], y = x.clone(), z = x.clone();
        for (int i = 0; i < x.length; i++) {
            x[i] = this.targets[i].x; y[i] = this.targets[i].y; z[i] = this.targets[i].z;
        }
        targetIndex = new FloatSpatialIndex(x, y, z);
    }
    public static <T> T run(Object owner, Map<TargetPathNode, BlockPos> targets, boolean vanillaEvaluator, Supplier<T> operation) {
        if (!vanillaEvaluator || targets.size() < 32 || !PerformanceSettings.PATH_INDEX)
            return operation.get();
        for (TargetPathNode target : targets.keySet())
            if (target.getClass() != TargetPathNode.class) return operation.get();
        PathSearchBatch previous = CURRENT.get();
        CURRENT.set(new PathSearchBatch(owner, targets.keySet()));
        try { return operation.get(); }
        finally { if (previous == null) CURRENT.remove(); else CURRENT.set(previous); }
    }
    public static Float calculate(Object owner, PathNode node) {
        PathSearchBatch batch = CURRENT.get();
        if (batch == null || batch.owner != owner || !batch.enabled) return null;
        if (node.getClass() != PathNode.class || batch.evaluated.size() >= 65536) {
            batch.finish(); batch.enabled = false; return null;
        }
        batch.evaluated.add(node);
        return batch.targetIndex.distanceToNearest(node.x, node.y, node.z);
    }
    public static void finish(Object owner) {
        PathSearchBatch batch = CURRENT.get();
        if (batch != null && batch.owner == owner && batch.enabled) batch.finish();
    }
    private void finish() {
        if (finished || evaluated.isEmpty()) return;
        finished = true;
        int[] x = new int[evaluated.size()], y = x.clone(), z = x.clone();
        for (int i = 0; i < x.length; i++) {
            PathNode node = evaluated.get(i); x[i] = node.x; y[i] = node.y; z[i] = node.z;
        }
        FloatSpatialIndex nodes = new FloatSpatialIndex(x, y, z);
        int[] tx = new int[targets.length], ty = tx.clone(), tz = tx.clone(), nearest = tx.clone();
        for (int i = 0; i < targets.length; i++) { tx[i] = targets[i].x; ty[i] = targets[i].y; tz[i] = targets[i].z; }
        ComputeService.forEach(targets.length, 4096, (from, to) -> {
            for (int i = from; i < to; i++) nearest[i] = nodes.nearest(tx[i], ty[i], tz[i]);
        });
        // Match vanilla's first-evaluation tie and mutate each target on the calling thread.
        for (int i = 0; i < targets.length; i++) {
            PathNode node = evaluated.get(nearest[i]);
            targets[i].updateNearestNode(FloatSpatialIndex.distance(tx[i], ty[i], tz[i], node.x, node.y, node.z), node);
        }
    }
}
