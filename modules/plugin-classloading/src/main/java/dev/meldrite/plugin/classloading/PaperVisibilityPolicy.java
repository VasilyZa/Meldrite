package dev.meldrite.plugin.classloading;
import dev.meldrite.plugin.spi.VisibilityPolicy;
import java.util.*;
public final class PaperVisibilityPolicy implements VisibilityPolicy {
    private final Map<String,List<String>> graph;
    public PaperVisibilityPolicy(Map<String,? extends Collection<String>> graph){Map<String,List<String>> copy=new LinkedHashMap<>();graph.forEach((key,value)->copy.put(key,List.copyOf(value)));this.graph=Map.copyOf(copy);}
    public List<String> routes(String consumer,Collection<String> available){LinkedHashSet<String> reachable=new LinkedHashSet<>();ArrayDeque<String> pending=new ArrayDeque<>();pending.add(consumer);while(!pending.isEmpty()){String current=pending.remove();if(!reachable.add(current))continue;pending.addAll(graph.getOrDefault(current,List.of()));}reachable.remove(consumer);reachable.retainAll(available);return List.copyOf(reachable);}
    public boolean exportLibraries(String consumer,String provider){return routes(consumer,List.of(provider)).contains(provider);}
    public void accessed(String consumer,String provider,String symbol){ }
}
