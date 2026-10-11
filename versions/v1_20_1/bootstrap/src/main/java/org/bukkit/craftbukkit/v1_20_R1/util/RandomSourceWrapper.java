package org.bukkit.craftbukkit.v1_20_R1.util;

import java.util.Objects;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.RandomSplitter;

public final class RandomSourceWrapper implements Random {
    private final java.util.Random random;
    public RandomSourceWrapper(java.util.Random random) { this.random = Objects.requireNonNull(random); }
    @Override public Random split() { return Random.create(nextLong()); }
    @Override public RandomSplitter nextSplitter() { return Random.create(nextLong()).nextSplitter(); }
    @Override public void setSeed(long seed) { random.setSeed(seed); }
    @Override public int nextInt() { return random.nextInt(); }
    @Override public int nextInt(int bound) { return random.nextInt(bound); }
    @Override public long nextLong() { return random.nextLong(); }
    @Override public boolean nextBoolean() { return random.nextBoolean(); }
    @Override public float nextFloat() { return random.nextFloat(); }
    @Override public double nextDouble() { return random.nextDouble(); }
    @Override public double nextGaussian() { return random.nextGaussian(); }
    public static final class RandomWrapper extends java.util.Random {
        private final Random random;
        public RandomWrapper(Random random) { this.random = Objects.requireNonNull(random); }
        @Override public void setSeed(long seed) { if (random != null) random.setSeed(seed); }
        @Override protected int next(int bits) { return random.nextInt() >>> (32 - bits); }
        @Override public int nextInt() { return random.nextInt(); }
        @Override public int nextInt(int bound) { return random.nextInt(bound); }
        @Override public long nextLong() { return random.nextLong(); }
        @Override public boolean nextBoolean() { return random.nextBoolean(); }
        @Override public float nextFloat() { return random.nextFloat(); }
        @Override public double nextDouble() { return random.nextDouble(); }
        @Override public double nextGaussian() { return random.nextGaussian(); }
    }
}
