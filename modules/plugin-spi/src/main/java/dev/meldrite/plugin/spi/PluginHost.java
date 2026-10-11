package dev.meldrite.plugin.spi;
/** Profile-owned callbacks; the common controller commits states only after they succeed. */
public interface PluginHost {
    PluginIdentity identity();
    PluginScope scope();
    default java.util.Optional<Throwable> preparationFailure(){return java.util.Optional.empty();}
    void construct() throws Exception;
    void load() throws Exception;
    void enable() throws Exception;
    void disable() throws Exception;
}
