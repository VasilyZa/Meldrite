package dev.meldrite.paper.map;

import java.awt.Color;

/** The original double-precision matching algorithm over an exclusive pixel range. */
final class ScalarMapColorConverter implements VectorMapColors.Converter {
    @Override public int vectorBits() { return 0; }

    @Override public void convert(int[] pixels, Color[] palette, byte[] result, int from, int to) {
        for (int index = from; index < to; index++) result[index] = pixel(pixels[index], palette);
    }

    static byte pixel(int pixel, Color[] palette) {
        if ((pixel >>> 24) < 128) return 0;
        int red = (pixel >>> 16) & 255, green = (pixel >>> 8) & 255, blue = pixel & 255, selected = 0;
        double best = Double.MAX_VALUE;
        for (int index = 4; index < palette.length; index++) {
            Color candidate = palette[index];
            double mean = (red + candidate.getRed()) / 2.0;
            double dr = red - candidate.getRed(), dg = green - candidate.getGreen(), db = blue - candidate.getBlue();
            double distance = (2 + mean / 256) * dr * dr + 4 * dg * dg + (2 + (255 - mean) / 256) * db * db;
            if (distance < best) { best = distance; selected = index; }
        }
        return (byte) selected;
    }
}
