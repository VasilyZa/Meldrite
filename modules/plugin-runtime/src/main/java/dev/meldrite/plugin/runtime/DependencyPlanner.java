package dev.meldrite.plugin.runtime;

import dev.meldrite.foundation.Diagnostic;
import dev.meldrite.plugin.spi.*;
import java.util.*;

/** Availability, ordering and visibility are distinct graphs; all output order is deterministic. */
public final class DependencyPlanner {
    public LoadPlan plan(List<PluginDescriptor> descriptors,DependencyRules rules){
        Objects.requireNonNull(rules);List<PluginDescriptor> plugins=List.copyOf(descriptors);Map<String,PluginDescriptor> providers=new LinkedHashMap<>();
        for(PluginDescriptor plugin:plugins){
            register(providers,plugin.identity().id(),plugin);
            for(String alias:plugin.provides())register(providers,alias,plugin);
        }
        List<Diagnostic> diagnostics=new ArrayList<>();Map<String,Set<String>> required=new LinkedHashMap<>();plugins.forEach(p->required.put(p.identity().id(),new LinkedHashSet<>()));
        PhaseResult bootstrap=phase(plugins,providers,rules,DependencyEdge.Phase.BOOTSTRAP,diagnostics,required);
        PhaseResult server=phase(plugins,providers,rules,DependencyEdge.Phase.SERVER,diagnostics,required);
        return new LoadPlan(bootstrap.order,server.order,bootstrap.visibility,server.visibility,required,diagnostics);
    }
    private static void register(Map<String,PluginDescriptor> providers,String name,PluginDescriptor plugin){
        PluginDescriptor prior=providers.putIfAbsent(name,plugin);
        if(prior!=null)throw new IllegalStateException("DUPLICATE_PROVIDER: "+name+" from "+prior.identity().id()+" and "+plugin.identity().id());
    }
    private record PhaseResult(List<String> order,Map<String,Set<String>> visibility) {}
    private PhaseResult phase(List<PluginDescriptor> plugins,Map<String,PluginDescriptor> providers,DependencyRules rules,DependencyEdge.Phase phase,List<Diagnostic> diagnostics,Map<String,Set<String>> required){
        Map<String,Set<String>> graph=new LinkedHashMap<>(),visibility=new LinkedHashMap<>();Map<String,Integer> priority=new HashMap<>();
        for(PluginDescriptor plugin:plugins)if(phase==DependencyEdge.Phase.SERVER||plugin.bootstrapper().isPresent()){
            String id=plugin.identity().id();graph.put(id,new LinkedHashSet<>());visibility.put(id,new LinkedHashSet<>());priority.put(id,plugin.discoveryIndex());
        }
        for(PluginDescriptor plugin:plugins){
            String id=plugin.identity().id();if(!graph.containsKey(id))continue;
            Set<DependencyEdge> available=new HashSet<>();
            for(DependencyEdge edge:plugin.dependencies()){
                if(edge.phase()!=phase)continue;PluginDescriptor target=providers.get(edge.target());
                boolean exists=target!=null&&graph.containsKey(target.identity().id());
                if(exists&&edge.versionRequirement().isPresent()&&!rules.versionMatches().test(edge.versionRequirement().get(),target.identity().version())){
                    if(edge.required())throw new IllegalStateException("DEPENDENCY_VERSION_MISMATCH: "+id+" requires "+edge.target()+" "+edge.versionRequirement().get()+", actual="+target.identity().version());
                    diagnostics.add(diagnostic("OPTIONAL_VERSION_MISMATCH",phase,id+" -> "+edge.target()));continue;
                }
                if(!exists){if(edge.required())throw new IllegalStateException("MISSING_REQUIRED_DEPENDENCY: "+phase+" "+id+" -> "+edge.target());continue;}
                available.add(edge);String targetId=target.identity().id();
                if(edge.required())required.get(targetId).add(id);
                if(edge.joinClasspath())visibility.get(id).add(targetId);
            }
            for(DependencyEdge edge:rules.orderingPolicy().edges(plugin,plugins)){
                if(edge.phase()!=phase||!available.contains(edge))continue;String target=providers.get(edge.target()).identity().id();
                if(edge.ordering()==DependencyEdge.Ordering.BEFORE_CONSUMER)graph.get(target).add(id);
                else if(edge.ordering()==DependencyEdge.Ordering.AFTER_CONSUMER)graph.get(id).add(target);
            }
        }
        for(;;){List<String> cycle=findCycle(graph);if(cycle.isEmpty())break;String path=String.join(" -> ",cycle);
            if(rules.cyclePolicy()==DependencyRules.CyclePolicy.REJECT)throw new IllegalStateException("DEPENDENCY_CYCLE: "+phase+" "+path);
            graph.get(cycle.get(cycle.size()-2)).remove(cycle.get(cycle.size()-1));diagnostics.add(diagnostic("ORDER_CYCLE",phase,path));
        }
        Map<String,Integer> degrees=new HashMap<>();graph.keySet().forEach(id->degrees.put(id,0));graph.values().forEach(edges->edges.forEach(id->degrees.merge(id,1,Integer::sum)));
        PriorityQueue<String> ready=new PriorityQueue<>(Comparator.<String>comparingInt(priority::get).thenComparing(Comparator.naturalOrder()));degrees.forEach((id,degree)->{if(degree==0)ready.add(id);});
        List<String> order=new ArrayList<>();while(!ready.isEmpty()){String id=ready.remove();order.add(id);for(String successor:graph.get(id))if(degrees.merge(successor,-1,Integer::sum)==0)ready.add(successor);}
        if(order.size()!=graph.size())throw new IllegalStateException("DEPENDENCY_GRAPH_INCOMPLETE: "+phase);
        return new PhaseResult(List.copyOf(order),visibility);
    }
    private static Diagnostic diagnostic(String code,DependencyEdge.Phase phase,String message){return new Diagnostic(code,Optional.empty(),phase.name(),message,null);}
    private static List<String> findCycle(Map<String,Set<String>> graph){Set<String> done=new HashSet<>();List<String> path=new ArrayList<>();for(String id:graph.keySet()){List<String> cycle=visit(id,graph,done,path);if(!cycle.isEmpty())return cycle;}return List.of();}
    private static List<String> visit(String id,Map<String,Set<String>> graph,Set<String> done,List<String> path){int index=path.indexOf(id);if(index>=0){List<String> cycle=new ArrayList<>(path.subList(index,path.size()));cycle.add(id);return cycle;}if(done.contains(id))return List.of();path.add(id);for(String next:graph.get(id)){List<String> cycle=visit(next,graph,done,path);if(!cycle.isEmpty())return cycle;}path.remove(path.size()-1);done.add(id);return List.of();}
}
