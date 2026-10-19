package dev.meldrite.v1201.perf;

/** A contiguous cell range, scattered back into the original player's result column. */
interface DistanceKernel {
    default int lanes() { return 1; }

    DistanceKernel SCALAR = (ex, ez, range, x, z, limits, indices, from, to, result) -> {
        for (int i = from; i < to; i++) {
            double dx = x[i] - ex, dz = z[i] - ez;
            double limit = Math.min(range, limits[i]);
            result[indices[i]] = dx * dx + dz * dz <= limit * limit;
        }
    };

    void nearby(double ex, double ez, int range, double[] x, double[] z, double[] limits,
                int[] indices, int from, int to, boolean[] result);
}
