package dev.meldrite.paper;
import dev.meldrite.plugin.spi.VisibilityPolicy;
import java.util.*;
/** One realm is retained while the pinned profile switches bootstrap/server visibility. */
final class PhaseVisibilityPolicy implements VisibilityPolicy {
    private volatile VisibilityPolicy current;
    private final Set<String> excluded=java.util.concurrent.ConcurrentHashMap.newKeySet();
    PhaseVisibilityPolicy(VisibilityPolicy initial){current=Objects.requireNonNull(initial);}
    void use(VisibilityPolicy policy){current=Objects.requireNonNull(policy);}
    public List<String> routes(String consumer,Collection<String> available){return current.routes(consumer,available).stream().filter(id->!excluded.contains(id)).toList();}
    public boolean exportLibraries(String consumer,String provider){return !excluded.contains(provider)&&current.exportLibraries(consumer,provider);}
    void exclude(String id){excluded.add(id);}
    void include(String id){excluded.remove(id);}
    public void accessed(String consumer,String provider,String symbol){current.accessed(consumer,provider,symbol);}
}
