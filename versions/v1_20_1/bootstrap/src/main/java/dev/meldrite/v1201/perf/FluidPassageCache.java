package dev.meldrite.v1201.perf;

/**
 * A thread-confined, fixed-capacity identity cache for ordered block-state pairs.
 * Four entries share each bucket; misses replace entries round-robin, while hits
 * only read arrays. Neither lookup nor insertion creates a composite key.
 */
public final class FluidPassageCache<T> {
    public static final int MISS = -1;
    private static final int WAYS = 4;
    private static final int BUCKETS = 64;
    private final Object[] fromStates = new Object[BUCKETS * WAYS];
    private final Object[] toStates = new Object[BUCKETS * WAYS];
    private final byte[] faces = new byte[BUCKETS * WAYS];
    private final byte[] results = new byte[BUCKETS * WAYS];
    private final byte[] nextEntries = new byte[BUCKETS];

    public int get(T fromState, T toState, int face) {
        int first = bucket(fromState, toState, face) * WAYS;
        for (int entry = first; entry < first + WAYS; entry++) {
            if (fromStates[entry] == fromState && toStates[entry] == toState && faces[entry] == face) {
                return results[entry];
            }
        }
        return MISS;
    }

    public void put(T fromState, T toState, int face, boolean result) {
        int bucket = bucket(fromState, toState, face);
        int first = bucket * WAYS;
        for (int entry = first; entry < first + WAYS; entry++) {
            if (fromStates[entry] == fromState && toStates[entry] == toState && faces[entry] == face) {
                results[entry] = (byte) (result ? 1 : 0);
                return;
            }
        }
        int entry = first + nextEntries[bucket];
        nextEntries[bucket] = (byte) ((nextEntries[bucket] + 1) & (WAYS - 1));
        fromStates[entry] = fromState;
        toStates[entry] = toState;
        faces[entry] = (byte) face;
        results[entry] = (byte) (result ? 1 : 0);
    }

    private static int bucket(Object fromState, Object toState, int face) {
        int hash = System.identityHashCode(fromState);
        hash = hash * 31 + System.identityHashCode(toState);
        hash = hash * 31 + face;
        hash ^= hash >>> 16;
        return hash & (BUCKETS - 1);
    }
}
