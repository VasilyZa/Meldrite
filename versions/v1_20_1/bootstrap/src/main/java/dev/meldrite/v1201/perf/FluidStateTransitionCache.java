package dev.meldrite.v1201.perf;

/** Thread-confined bounded cache of successful transitions of immutable states. */
public final class FluidStateTransitionCache<T> {
    private static final int CAPACITY = 128;
    private final Object[] sourceStates = new Object[CAPACITY];
    private final Object[] targetStates = new Object[CAPACITY];
    private final byte[] transitions = new byte[CAPACITY];

    @SuppressWarnings("unchecked")
    public T get(T state, int transition) {
        int entry = entry(state, transition);
        return sourceStates[entry] == state && transitions[entry] == transition
                ? (T) targetStates[entry] : null;
    }

    public void put(T state, int transition, T result) {
        int entry = entry(state, transition);
        sourceStates[entry] = state;
        transitions[entry] = (byte) transition;
        targetStates[entry] = result;
    }

    private static int entry(Object state, int transition) {
        int hash = System.identityHashCode(state) * 31 + transition;
        hash ^= hash >>> 16;
        return hash & (CAPACITY - 1);
    }
}
