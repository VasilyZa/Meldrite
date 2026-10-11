package dev.meldrite.paper.compat;

/** Legacy reflection target bound to the live Fabric server. */
public final class DedicatedServerFacade {
    private DedicatedServerFacade() {}
    private static volatile Object server;
    public static void bind(Object value) { server = java.util.Objects.requireNonNull(value, "server"); }
    public static Object getServer() {
        if (server == null) throw new IllegalStateException("DEDICATED_SERVER_NOT_BOUND");
        return server;
    }
}
