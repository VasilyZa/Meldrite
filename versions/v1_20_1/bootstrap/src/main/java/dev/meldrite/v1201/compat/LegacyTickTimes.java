package dev.meldrite.v1201.compat;

/** Real rolling nanosecond samples exposed through the live MinecraftServer mixin. */
public final class LegacyTickTimes {
    private final long[] samples;
    private int cursor;

    public LegacyTickTimes(int size) { samples = new long[size]; }

    public synchronized void add(long nanos) { samples[cursor++ % samples.length] = nanos; }

    public synchronized long[] getTimes() { return samples.clone(); }
}
