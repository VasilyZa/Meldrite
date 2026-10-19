package dev.meldrite.v1201.perf;

import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorShape;
import jdk.incubator.vector.VectorSpecies;

/** Local-palette integer gathers avoid repeating native state lookups during packing. */
public final class VectorCollisionKernel implements CollisionDataPacking.Kernel {
    // The intermediate is an int: at most sixteen two-bit fields may share one reduction.
    private static final VectorSpecies<Integer> SPECIES = VectorSpecies.of(int.class,
            VectorShape.forBitSize(Math.min(512, IntVector.SPECIES_PREFERRED.vectorBitSize())));
    private static final IntVector SHIFTS = IntVector.zero(SPECIES).addIndex(2);

    @Override public int pack(int[] metadata, int[] indices, long[] result) {
        int special = 0;
        for (int word = 0; word < result.length; word++) {
            long bits = 0;
            for (int lane = 0; lane < 32; lane += SPECIES.length()) {
                var values = IntVector.fromArray(SPECIES, metadata, 0, indices, (word << 5) + lane);
                int packed = values.and(3).lanewise(VectorOperators.LSHL, SHIFTS).reduceLanes(VectorOperators.OR);
                bits |= Integer.toUnsignedLong(packed) << (lane << 1);
                special += values.lanewise(VectorOperators.LSHR, 2).and(1).reduceLanes(VectorOperators.ADD);
            }
            result[word] = bits;
        }
        return special;
    }
}
