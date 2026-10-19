package dev.meldrite.v1201.perf;

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;

/** Only snapshot arithmetic is vectorized; entity tracking itself stays on its original thread. */
public final class VectorDistanceKernel implements DistanceKernel {
    private static final VectorSpecies<Double> SPECIES = DoubleVector.SPECIES_PREFERRED;

    @Override public int lanes() { return SPECIES.length(); }

    @Override public void nearby(double ex, double ez, int range, double[] x, double[] z, double[] limits,
                                 int[] indices, int from, int to, boolean[] result) {
        int bound = from + SPECIES.loopBound(to - from), index = from;
        for (; index < bound; index += SPECIES.length()) {
            var dx = DoubleVector.fromArray(SPECIES, x, index).sub(ex);
            var dz = DoubleVector.fromArray(SPECIES, z, index).sub(ez);
            var limit = DoubleVector.fromArray(SPECIES, limits, index).min(range);
            long mask = dx.mul(dx).add(dz.mul(dz)).compare(VectorOperators.LE, limit.mul(limit)).toLong();
            for (int lane = 0; lane < SPECIES.length(); lane++)
                result[indices[index + lane]] = (mask & (1L << lane)) != 0;
        }
        DistanceKernel.SCALAR.nearby(ex, ez, range, x, z, limits, indices, index, to, result);
    }
}
