package dev.meldrite.plugin.runtime;
import dev.meldrite.plugin.spi.*;
import java.util.*;
/** The same scope authority is shared by scheduler, listeners, commands and services. */
public final class ScopeRegistry {
    private final Map<String,PluginScope> scopes;
    public ScopeRegistry(Map<String,PluginScope> scopes){this.scopes=Map.copyOf(scopes);}
    public PluginScope scope(String owner){PluginScope scope=scopes.get(owner);if(scope==null)throw new IllegalArgumentException("UNKNOWN_PLUGIN_OWNER: "+owner);return scope;}
    public <T extends AutoCloseable> T register(String owner,T resource){return scope(owner).register(resource);}
    public boolean closed(String owner){return scope(owner).isClosed();}
}
