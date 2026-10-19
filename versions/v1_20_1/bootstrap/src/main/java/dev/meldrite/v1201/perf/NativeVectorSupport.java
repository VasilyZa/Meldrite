package dev.meldrite.v1201.perf;

/** Optional kernels never make the ordinary server require the incubator module. */
final class NativeVectorSupport {
    private NativeVectorSupport() { }

    static <T> T load(String name, Class<T> contract, T scalar) {
        if (Boolean.getBoolean("meldrite.vector.disabled")
                || ModuleLayer.boot().findModule("jdk.incubator.vector").isEmpty()) return scalar;
        try {
            return contract.cast(Class.forName(name, true, NativeVectorSupport.class.getClassLoader())
                    .getConstructor().newInstance());
        } catch (ReflectiveOperationException | LinkageError unavailable) {
            return scalar;
        }
    }
}
