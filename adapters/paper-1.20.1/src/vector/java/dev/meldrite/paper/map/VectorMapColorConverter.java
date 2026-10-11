package dev.meldrite.paper.map;

import java.awt.Color;
import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorShape;
import jdk.incubator.vector.VectorSpecies;

/** Batches independent pixels while retaining Bukkit's double precision and tie ordering. */
public final class VectorMapColorConverter implements VectorMapColors.Converter {
    private static final VectorSpecies<Double> DOUBLES = DoubleVector.SPECIES_PREFERRED;
    private static final VectorSpecies<Integer> INTEGERS = VectorSpecies.of(int.class,
            VectorShape.forBitSize(Math.max(64, DOUBLES.vectorBitSize() / 2)));

    @Override public int vectorBits() { return DOUBLES.vectorBitSize(); }

    @Override public void convert(int[] pixels, Color[] palette, byte[] result, int from, int to) {
        int limit = from + DOUBLES.loopBound(to - from), index = from;
        for (; index < limit; index += DOUBLES.length()) {
            var packed = IntVector.fromArray(INTEGERS, pixels, index);
            var red = channel(packed, 16);
            var green = channel(packed, 8);
            var blue = channel(packed, 0);
            var best = DoubleVector.broadcast(DOUBLES, Double.MAX_VALUE);
            var selected = DoubleVector.zero(DOUBLES);
            for (int color = 4; color < palette.length; color++) {
                Color candidate = palette[color];
                var mean = red.add(candidate.getRed()).div(2);
                var dr = red.sub(candidate.getRed());
                var dg = green.sub(candidate.getGreen());
                var db = blue.sub(candidate.getBlue());
                var redWeight = mean.div(256).add(2);
                var blueWeight = DoubleVector.broadcast(DOUBLES, 255).sub(mean).div(256).add(2);
                var distance = redWeight.mul(dr).mul(dr).add(dg.mul(4).mul(dg)).add(blueWeight.mul(db).mul(db));
                var closer = distance.compare(VectorOperators.LT, best);
                best = best.blend(distance, closer);
                selected = selected.blend(color, closer);
            }
            for (int lane = 0; lane < DOUBLES.length(); lane++)
                result[index + lane] = (pixels[index + lane] >>> 24) < 128 ? 0 : (byte) selected.lane(lane);
        }
        for (; index < to; index++) result[index] = ScalarMapColorConverter.pixel(pixels[index], palette);
    }

    private static DoubleVector channel(IntVector pixels, int shift) {
        return (DoubleVector) pixels.lanewise(VectorOperators.LSHR, shift).and(255)
                .convertShape(VectorOperators.I2D, DOUBLES, 0);
    }

}
