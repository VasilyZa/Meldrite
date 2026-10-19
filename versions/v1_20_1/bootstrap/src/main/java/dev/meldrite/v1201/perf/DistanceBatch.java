package dev.meldrite.v1201.perf;

import dev.meldrite.scheduler.ComputeService;
import java.util.HashMap;
import java.util.Map;

/** Workers query an immutable player grid over primitive snapshots. */
public final class DistanceBatch {
    private static final double CELL = 128.0;
    private static final DistanceKernel KERNEL = NativeVectorSupport.load(
            "dev.meldrite.v1201.perf.VectorDistanceKernel", DistanceKernel.class, DistanceKernel.SCALAR);
    private static final int VECTOR_LANES = KERNEL.lanes();
    private static final class Cell {
        int size, from, to, cursor;
    }
    private DistanceBatch() { }
    private static long key(int x, int z) { return ((long) x << 32) | (z & 0xffffffffL); }
    public static boolean[][] tracking(double[] entityX, double[] entityZ, int[] range,
                                        double[] playerX, double[] playerZ, int[] viewDistance) {
        if (entityX.length != entityZ.length || entityX.length != range.length
                || playerX.length != playerZ.length || playerX.length != viewDistance.length)
            throw new IllegalArgumentException("Position snapshot dimensions differ");
        boolean[][] result = new boolean[entityX.length][playerX.length];
        if (playerX.length == 0) return result;
        double[] limits = new double[playerX.length];
        int[] indices = new int[playerX.length];
        int maximumView = 0;
        boolean gridSafe = true;
        for (int i = 0; i < playerX.length; i++) {
            gridSafe &= Double.isFinite(playerX[i]) && Double.isFinite(playerZ[i])
                    && Math.abs(playerX[i]) < 1.0e9 && Math.abs(playerZ[i]) < 1.0e9
                    && viewDistance[i] >= 0 && viewDistance[i] <= 32;
            maximumView = Math.max(maximumView, viewDistance[i]);
            // Keep native integer multiplication, including its overflow on invalid views.
            limits[i] = viewDistance[i] * 16;
            indices[i] = i;
        }
        if (!gridSafe) {
            ComputeService.forEach(entityX.length, 128, (from, to) -> {
                for (int entity = from; entity < to; entity++)
                    KERNEL.nearby(entityX[entity], entityZ[entity], range[entity], playerX, playerZ, limits,
                            indices, 0, playerX.length, result[entity]);
            });
            return result;
        }
        Map<Long, Cell> grid = new HashMap<>();
        long[] cells = new long[playerX.length];
        for (int player = 0; player < playerX.length; player++) {
            cells[player] = key((int) Math.floor(playerX[player] / CELL), (int) Math.floor(playerZ[player] / CELL));
            grid.computeIfAbsent(cells[player], ignored -> new Cell()).size++;
        }
        int offset = 0;
        for (Cell cell : grid.values()) {
            cell.from = cell.cursor = offset;
            offset += cell.size;
            cell.to = offset;
        }
        double[] packedX = new double[playerX.length], packedZ = new double[playerX.length];
        double[] packedLimits = new double[playerX.length];
        for (int player = 0; player < playerX.length; player++) {
            int destination = grid.get(cells[player]).cursor++;
            packedX[destination] = playerX[player]; packedZ[destination] = playerZ[player];
            packedLimits[destination] = limits[player]; indices[destination] = player;
        }
        final int maxView = maximumView;
        ComputeService.forEach(entityX.length, 128, (from, to) -> {
            for (int entity = from; entity < to; entity++) {
                double ex = entityX[entity], ez = entityZ[entity];
                int limit = Math.min(range[entity], maxView * 16);
                if (limit < 0 || !Double.isFinite(ex) || !Double.isFinite(ez)
                        || Math.abs(ex) >= 1.0e9 || Math.abs(ez) >= 1.0e9) {
                    KERNEL.nearby(ex, ez, range[entity], packedX, packedZ, packedLimits,
                            indices, 0, playerX.length, result[entity]);
                    continue;
                }
                int minX = (int) Math.floor(Math.nextDown(ex - limit) / CELL), maxX = (int) Math.floor(Math.nextUp(ex + limit) / CELL);
                int minZ = (int) Math.floor(Math.nextDown(ez - limit) / CELL), maxZ = (int) Math.floor(Math.nextUp(ez + limit) / CELL);
                long visitedCells = (long) (maxX - minX + 1) * (maxZ - minZ + 1);
                // For a bounded roster, a hash lookup costs more than a couple of vector batches.
                // Tight queries and large rosters still use the spatial grid rather than all pairs.
                if (VECTOR_LANES > 1 && playerX.length <= VECTOR_LANES * 64
                        && visitedCells * VECTOR_LANES * 2 >= playerX.length) {
                    KERNEL.nearby(ex, ez, range[entity], packedX, packedZ, packedLimits,
                            indices, 0, playerX.length, result[entity]);
                    continue;
                }
                for (int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++) {
                    Cell players = grid.get(key(x, z));
                    if (players != null)
                        KERNEL.nearby(ex, ez, range[entity], packedX, packedZ, packedLimits,
                                indices, players.from, players.to, result[entity]);
                }
            }
        });
        return result;
    }
}
