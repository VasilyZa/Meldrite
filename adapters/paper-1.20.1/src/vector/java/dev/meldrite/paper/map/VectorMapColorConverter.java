package dev.meldrite.paper.map;

import java.awt.Color;
import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorShape;
import jdk.incubator.vector.VectorSpecies;

/** Batches independent pixels while retaining Bukkit's double precision and tie ordering. */
public final class VectorMapColorConverter implements VectorMapColors.MatchingConverter {
    private static final VectorSpecies<Double> DOUBLES = DoubleVector.SPECIES_PREFERRED;
    private static final VectorSpecies<Integer> INTEGERS = VectorSpecies.of(int.class,
            VectorShape.forBitSize(Math.max(64, DOUBLES.vectorBitSize() / 2)));
    private static final VectorSpecies<Integer> MATCHING = IntVector.SPECIES_PREFERRED;
    private static final IntVector COLOR_OFFSETS = IntVector.zero(MATCHING).addIndex(1);

    @Override public int vectorBits() { return DOUBLES.vectorBitSize(); }

    @Override public void convert(int[] pixels, Color[] palette, byte[] result, int from, int to) {
        var colors = VectorMapColors.paletteSnapshot(palette);
        if (colors == null) {
            for (int index = from; index < to; index++) result[index] = ScalarMapColorConverter.pixel(pixels[index], palette);
            return;
        }
        int limit = from + DOUBLES.loopBound(to - from), index = from;
        for (; index < limit; index += DOUBLES.length()) {
            var packed = IntVector.fromArray(INTEGERS, pixels, index);
            if (packed.lanewise(VectorOperators.LSHR, 24).compare(VectorOperators.LT, 128).allTrue()) {
                java.util.Arrays.fill(result, index, index + DOUBLES.length(), (byte) 0);
                continue;
            }
            var red = channel(packed, 16);
            var green = channel(packed, 8);
            var blue = channel(packed, 0);
            var best = DoubleVector.broadcast(DOUBLES, Double.MAX_VALUE);
            var selected = DoubleVector.zero(DOUBLES);
            for (int color = 4; color < palette.length; color++) {
                int candidateRed = colors.red[color], candidateGreen = colors.green[color], candidateBlue = colors.blue[color];
                var mean = red.add(candidateRed).mul(0.5);
                var dr = red.sub(candidateRed);
                var dg = green.sub(candidateGreen);
                var db = blue.sub(candidateBlue);
                // All operands are bounded 8-bit channels; these binary fractions are exact.
                var redWeight = mean.mul(1.0 / 256).add(2);
                var blueWeight = DoubleVector.broadcast(DOUBLES, 255).sub(mean).mul(1.0 / 256).add(2);
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

    @Override public int matchColor(int red, int green, int blue, int[] paletteRed, int[] paletteGreen, int[] paletteBlue) {
        var r = IntVector.broadcast(MATCHING, red);
        var g = IntVector.broadcast(MATCHING, green);
        var b = IntVector.broadcast(MATCHING, blue);
        var best = IntVector.broadcast(MATCHING, Integer.MAX_VALUE);
        var selected = IntVector.broadcast(MATCHING, Integer.MAX_VALUE);
        int limit = 4 + MATCHING.loopBound(Math.max(0, paletteRed.length - 4)), index = 4;
        for (; index < limit; index += MATCHING.length()) {
            var pr = IntVector.fromArray(MATCHING, paletteRed, index);
            var pg = IntVector.fromArray(MATCHING, paletteGreen, index);
            var pb = IntVector.fromArray(MATCHING, paletteBlue, index);
            var sum = r.add(pr);
            var dr = r.sub(pr); var dg = g.sub(pg); var db = b.sub(pb);
            // 512 * Paper's distance is an exact integer in [0, 299505150].
            // Retaining the scale avoids rounding changes and doubles AVX2's lane count.
            var distance = dr.mul(dr).mul(sum.add(1024)).add(dg.mul(dg).lanewise(VectorOperators.LSHL, 11))
                    .add(db.mul(db).mul(IntVector.broadcast(MATCHING, 1534).sub(sum)));
            var closer = distance.compare(VectorOperators.LT, best);
            best = best.blend(distance, closer);
            selected = selected.blend(COLOR_OFFSETS.add(index), closer);
        }
        int bestDistance = Integer.MAX_VALUE, selectedIndex = 0;
        if (index > 4) {
            bestDistance = best.reduceLanes(VectorOperators.MIN);
            selectedIndex = selected.blend(Integer.MAX_VALUE, best.compare(VectorOperators.NE, bestDistance))
                    .reduceLanes(VectorOperators.MIN);
        }
        for (; index < paletteRed.length; index++) {
            int sum = red + paletteRed[index];
            int dr = red - paletteRed[index], dg = green - paletteGreen[index], db = blue - paletteBlue[index];
            int distance = (1024 + sum) * dr * dr + 2048 * dg * dg + (1534 - sum) * db * db;
            if (distance < bestDistance) { bestDistance = distance; selectedIndex = index; }
        }
        return selectedIndex;
    }

    private static DoubleVector channel(IntVector pixels, int shift) {
        return (DoubleVector) pixels.lanewise(VectorOperators.LSHR, shift).and(255)
                .convertShape(VectorOperators.I2D, DOUBLES, 0);
    }

}
