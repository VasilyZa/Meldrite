package dev.meldrite.v1201.perf;

import dev.meldrite.scheduler.ComputeService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/** Workers query an immutable player grid over primitive snapshots. */
public final class DistanceBatch {
    private static final double CELL = 128.0;
    private DistanceBatch() { }
    private static long key(int x, int z) { return ((long) x << 32) | (z & 0xffffffffL); }
    public static boolean[][] tracking(double[] entityX, double[] entityZ, int[] range,
                                        double[] playerX, double[] playerZ, int[] viewDistance) {
        if (entityX.length != entityZ.length || entityX.length != range.length
                || playerX.length != playerZ.length || playerX.length != viewDistance.length)
            throw new IllegalArgumentException("Position snapshot dimensions differ");
        boolean[][] result = new boolean[entityX.length][playerX.length];
        if (playerX.length == 0) return result;
        Map<Long, ArrayList<Integer>> building = new HashMap<>();
        int maximumView = 0;
        boolean gridSafe = true;
        for (int i = 0; i < playerX.length; i++) {
            gridSafe &= Double.isFinite(playerX[i]) && Double.isFinite(playerZ[i])
                    && Math.abs(playerX[i]) < 1.0e9 && Math.abs(playerZ[i]) < 1.0e9
                    && viewDistance[i] >= 0 && viewDistance[i] <= 32;
            maximumView = Math.max(maximumView, viewDistance[i]);
            building.computeIfAbsent(key((int) Math.floor(playerX[i] / CELL),
                    (int) Math.floor(playerZ[i] / CELL)), ignored -> new ArrayList<>()).add(i);
        }
        Map<Long, int[]> grid = new HashMap<>();
        building.forEach((cell, players) -> grid.put(cell, players.stream().mapToInt(Integer::intValue).toArray()));
        final boolean useGrid = gridSafe;
        final int maxView = maximumView;
        ComputeService.forEach(entityX.length, 128, (from, to) -> {
            for (int entity = from; entity < to; entity++) {
                double ex = entityX[entity], ez = entityZ[entity];
                int limit = Math.min(range[entity], maxView * 16);
                if (!useGrid || limit < 0 || !Double.isFinite(ex) || !Double.isFinite(ez)
                        || Math.abs(ex) >= 1.0e9 || Math.abs(ez) >= 1.0e9) {
                    for (int player = 0; player < playerX.length; player++)
                        result[entity][player] = nearby(ex, ez, range[entity], playerX[player], playerZ[player], viewDistance[player]);
                    continue;
                }
                int minX = (int) Math.floor(Math.nextDown(ex - limit) / CELL), maxX = (int) Math.floor(Math.nextUp(ex + limit) / CELL);
                int minZ = (int) Math.floor(Math.nextDown(ez - limit) / CELL), maxZ = (int) Math.floor(Math.nextUp(ez + limit) / CELL);
                for (int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++) {
                    int[] players = grid.get(key(x, z));
                    if (players != null) for (int player : players)
                        result[entity][player] = nearby(ex, ez, range[entity], playerX[player], playerZ[player], viewDistance[player]);
                }
            }
        });
        return result;
    }
    private static boolean nearby(double ex, double ez, int range, double px, double pz, int view) {
        double x = px - ex, z = pz - ez;
        double limit = Math.min(range, view * 16);
        return x * x + z * z <= limit * limit;
    }
}
