package dev.meldrite.v1201.perf;

/** Packs low two collision bits and counts bit two, independently of any live game object. */
public final class CollisionDataPacking {
    interface Kernel {
        int pack(int[] metadata, int[] indices, long[] result);
    }

    private static final Kernel KERNEL = NativeVectorSupport.load(
            "dev.meldrite.v1201.perf.VectorCollisionKernel", Kernel.class, CollisionDataPacking::scalar);

    private CollisionDataPacking() { }

    public static int pack(int[] metadata, int[] indices, long[] result) {
        if ((indices.length & 31) != 0 || result.length != (indices.length >>> 5))
            throw new IllegalArgumentException("Collision data needs one output word per 32 states");
        return KERNEL.pack(metadata, indices, result);
    }

    private static int scalar(int[] metadata, int[] indices, long[] result) {
        int special = 0;
        for (int word = 0; word < result.length; word++) {
            long bits = 0;
            for (int lane = 0; lane < 32; lane++) {
                int value = metadata[indices[(word << 5) + lane]];
                bits |= (long) (value & 3) << (lane << 1);
                special += (value >>> 2) & 1;
            }
            result[word] = bits;
        }
        return special;
    }
}
