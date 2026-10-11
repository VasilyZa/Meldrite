package dev.meldrite.plugin.classloading;
import dev.meldrite.plugin.spi.VisibilityPolicy;
import java.util.*;
import java.util.function.Consumer;
public final class LegacyVisibilityPolicy implements VisibilityPolicy {
    private final PaperVisibilityPolicy declared;
    private final Consumer<String> diagnostics;
    public LegacyVisibilityPolicy(Map<String,? extends Collection<String>> graph,Consumer<String> diagnostics){this.declared=new PaperVisibilityPolicy(graph);this.diagnostics=Objects.requireNonNull(diagnostics);}
    public List<String> routes(String consumer,Collection<String> available){LinkedHashSet<String> routes=new LinkedHashSet<>(declared.routes(consumer,available));routes.addAll(available);routes.remove(consumer);return List.copyOf(routes);}
    public boolean exportLibraries(String consumer,String provider){return declared.exportLibraries(consumer,provider);}
    public void accessed(String consumer,String provider,String symbol){if(!declared.routes(consumer,List.of(provider)).contains(provider))diagnostics.accept("UNDECLARED_PLUGIN_ACCESS: "+consumer+" -> "+provider+" "+symbol);}
}
