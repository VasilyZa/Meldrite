package dev.meldrite.v1201.compat;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtTagSizeTracker;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

/** Real 1.20.1 NBT decoding used by newer Mojang-mapped plugin branches. */
public final class NativeNbtIo {
    private NativeNbtIo() {}

    public static NbtCompound readCompressed(Path path, NbtTagSizeTracker tracker) throws IOException {
        try (DataInputStream input = new DataInputStream(new GZIPInputStream(Files.newInputStream(path)))) {
            return NbtIo.read(input, tracker);
        }
    }

    public static NbtTagSizeTracker unlimitedHeap() {
        return new NbtTagSizeTracker(Long.MAX_VALUE);
    }

    /** Preserves the legacy remove return value while using the native mutator. */
    public static NbtElement remove(NbtCompound compound, String key) {
        NbtElement removed = compound.get(key);
        compound.remove(key);
        return removed;
    }
}
