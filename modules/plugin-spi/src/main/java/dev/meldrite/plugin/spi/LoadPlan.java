package dev.meldrite.plugin.spi;
import dev.meldrite.foundation.Diagnostic;
import java.util.*;
public record LoadPlan(List<String> bootstrapOrder, List<String> serverOrder,
        Map<String,Set<String>> bootstrapVisibility, Map<String,Set<String>> visibilityGraph,
        Map<String,Set<String>> requiredDependents, List<Diagnostic> diagnostics) {
    public LoadPlan {
        bootstrapOrder=List.copyOf(bootstrapOrder);serverOrder=List.copyOf(serverOrder);diagnostics=List.copyOf(diagnostics);
        bootstrapVisibility=immutable(bootstrapVisibility);visibilityGraph=immutable(visibilityGraph);requiredDependents=immutable(requiredDependents);
    }
    private static Map<String,Set<String>> immutable(Map<String,Set<String>> map){Map<String,Set<String>> result=new LinkedHashMap<>();map.forEach((k,v)->result.put(k,Set.copyOf(v)));return Collections.unmodifiableMap(result);}
}
