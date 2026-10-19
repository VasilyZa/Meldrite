package dev.meldrite.v1201.perf;

import dev.meldrite.v1201.compat.NativeBlockStateCacheAccess;
import java.util.Arrays;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.world.chunk.PalettedContainer;

/** Classify each local palette once; no state classification is retained across rebuilds. */
public final class SectionCollisionData {
    private static final class Scratch {
        final int[] indices = new int[4096];
        boolean busy;
    }
    private static final ThreadLocal<Scratch> SCRATCH = ThreadLocal.withInitial(Scratch::new);

    private SectionCollisionData() { }

    public static int rebuild(PalettedContainer<BlockState> container, long[] result) {
        if (result.length != 128) throw new IllegalArgumentException("A section needs 128 collision words");
        // Read the volatile data only once so palette and storage always belong together.
        var data = container.data;
        var palette = data.palette();
        var storage = data.storage();
        int size = palette.getSize();
        // A global ID palette can contain many more states than this section. Do not scan it all.
        if (size < 1 || size > 256 || storage.getSize() != 4096) return scalar(container, result);
        int[] metadata = new int[size];
        for (int i = 0; i < size; i++) {
            BlockState state = palette.get(i);
            long behavior = ((NativeBlockStateCacheAccess) state).getBlockCollisionBehavior();
            if (behavior < 0 || behavior > 3) return scalar(container, result);
            metadata[i] = (int) behavior | (special(state) ? 4 : 0);
        }
        if (size == 1) {
            Arrays.fill(result, 0x5555555555555555L * (metadata[0] & 3));
            return (metadata[0] & 4) == 0 ? 0 : 4096;
        }
        Scratch cached = SCRATCH.get();
        Scratch scratch = cached.busy ? new Scratch() : cached;
        scratch.busy = true;
        try {
            storage.writePaletteIndices(scratch.indices);
            return CollisionDataPacking.pack(metadata, scratch.indices, result);
        } finally {
            scratch.busy = false;
        }
    }

    private static int scalar(PalettedContainer<BlockState> container, long[] result) {
        int special = 0;
        for (int word = 0; word < result.length; word++) {
            long bits = 0;
            for (int lane = 0; lane < 32; lane++) {
                int index = (word << 5) + lane;
                BlockState state = container.get(index & 15, index >>> 8, (index >>> 4) & 15);
                int shift = lane << 1;
                // Preserve the original read/modify/write semantics even for foreign cache values.
                bits = (bits & ~(3L << shift)) | (((NativeBlockStateCacheAccess) state).getBlockCollisionBehavior() << shift);
                if (special(state)) special++;
            }
            result[word] = bits;
        }
        return special;
    }

    private static boolean special(BlockState state) {
        return state.exceedsCube() || state.isOf(Blocks.MOVING_PISTON);
    }
}
