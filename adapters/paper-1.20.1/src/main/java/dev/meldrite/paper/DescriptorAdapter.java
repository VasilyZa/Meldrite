package dev.meldrite.paper;

import com.google.gson.Gson;
import dev.meldrite.plugin.spi.*;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.*;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.InvalidDescriptionException;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.MarkedYAMLException;

/** Syntax and metadata semantics for the pinned profile only; no lifecycle execution occurs here. */
public final class DescriptorAdapter implements DescriptorReader {
    private static final Gson JSON=new Gson();
    @Override public List<String> descriptorNames(){return List.of("paper-plugin.yml","plugin.yml");}
    @Override public PluginDescriptor read(Path source,Map<String,String> documents,int discoveryIndex){
        String origin=documents.containsKey("paper-plugin.yml")?"paper-plugin.yml":"plugin.yml";
        String text=documents.get(origin);if(text==null)throw failure(source,origin,1,"Descriptor missing",null);
        LoaderOptions options=new LoaderOptions();options.setAllowDuplicateKeys(false);options.setMaxAliasesForCollections(50);options.setNestingDepthLimit(50);options.setCodePointLimit(1_048_576);
        Map<String,Object> values;
        try{Object loaded=new Yaml(new SafeConstructor(options)).load(text);validateData(loaded,Collections.newSetFromMap(new IdentityHashMap<>()),new int[]{100_000});values=map(loaded,"root");}
        catch(MarkedYAMLException invalid){int line=invalid.getProblemMark()!=null?invalid.getProblemMark().getLine()+1:1;throw failure(source,origin,line,invalid.getProblem(),invalid);}
        catch(RuntimeException invalid){throw failure(source,origin,1,invalid.getMessage(),invalid);}
        try {
            if (origin.equals("paper-plugin.yml") && documents.containsKey("plugin.yml")) {
                values = mergeLegacyCompatibility(values, documents.get("plugin.yml"));
            }
            return origin.equals("paper-plugin.yml") ? paper(values, origin, discoveryIndex) : legacy(values, text, origin, discoveryIndex);
        }
        catch(IllegalArgumentException|InvalidDescriptionException invalid){throw failure(source,origin,1,invalid.getMessage(),invalid);}
    }

    /**
     * Paper plugins may ship both descriptors.  Paper owns the identity and
     * dependency model, while the legacy descriptor still carries Bukkit
     * command/permission metadata used by PluginCommand and older plugins.
     */
    private static Map<String,Object> mergeLegacyCompatibility(Map<String,Object> paperValues, String legacyText) {
        if (legacyText == null || legacyText.isBlank()) return paperValues;
        try {
            LoaderOptions options = new LoaderOptions();
            options.setAllowDuplicateKeys(false);
            options.setMaxAliasesForCollections(50);
            options.setNestingDepthLimit(50);
            options.setCodePointLimit(1_048_576);
            Object loaded = new Yaml(new SafeConstructor(options)).load(legacyText);
            Map<String,Object> legacy = map(loaded, "plugin.yml");
            Map<String,Object> merged = new LinkedHashMap<>(paperValues);
            for (String key : List.of("commands", "permissions", "depend", "softdepend", "loadbefore",
                    "libraries", "description", "authors", "author", "contributors", "website", "prefix", "provides")) {
                if (!merged.containsKey(key) && legacy.containsKey(key)) merged.put(key, legacy.get(key));
            }
            return merged;
        } catch (RuntimeException ignored) {
            // A valid paper-plugin.yml remains loadable when an optional legacy
            // descriptor is malformed; Paper metadata stays authoritative.
            return paperValues;
        }
    }
    private PluginDescriptor paper(Map<String,Object> values,String origin,int index){
        String name=string(values,"name",true),main=className(string(values,"main",true)),version=string(values,"version",true),api=string(values,"api-version",true);
        if(!name.matches("[A-Za-z0-9_.-]+")||Set.of("bukkit","minecraft","mojang","spigot","paper").contains(name.toLowerCase(Locale.ROOT)))throw new IllegalArgumentException("Invalid/reserved plugin name: "+name);
        if(!Set.of("1.19","1.20").contains(api))throw new IllegalArgumentException("Unsupported api-version for this profile: "+api);
        List<DependencyEdge> edges=new ArrayList<>();Object declaration=values.get("dependencies");
        Map<String,Object> phases=declaration instanceof Map<?,?>?map(declaration,"dependencies"):Map.of();
        for(var phase:phases.entrySet()){
            DependencyEdge.Phase kind=switch(phase.getKey()){case "bootstrap"->DependencyEdge.Phase.BOOTSTRAP;case "server"->DependencyEdge.Phase.SERVER;default->throw new IllegalArgumentException("Unknown dependency phase: "+phase.getKey());};
            for(var dependency:map(phase.getValue(),"dependencies."+phase.getKey()).entrySet()){
                Map<String,Object> flags=map(dependency.getValue(),"dependency "+dependency.getKey());String load=string(flags,"load",false);
                DependencyEdge.Ordering order=switch(load==null?"OMIT":load.toUpperCase(Locale.ROOT)){case "BEFORE"->DependencyEdge.Ordering.BEFORE_CONSUMER;case "AFTER"->DependencyEdge.Ordering.AFTER_CONSUMER;case "OMIT"->DependencyEdge.Ordering.NONE;default->throw new IllegalArgumentException("Invalid dependency load: "+load);};
                edges.add(new DependencyEdge(dependency.getKey(),kind,bool(flags,"required",true),order,bool(flags,"join-classpath",true),origin,Optional.empty()));
            }
        }
        if(!(declaration instanceof Map<?,?>))edges.addAll(historicDependencies(values,origin));
        return new PluginDescriptor(new PluginIdentity(name,version),PluginDescriptor.Kind.PAPER,main,origin,
                optionalClass(values,"bootstrapper"),optionalClass(values,"loader"),enablePhase(string(values,"load",false)),strings(values,"provides"),edges,properties(values),index);
    }
    private enum HistoricFlag { BEFORE, AFTER, DEPENDENCY, REQUIRED }
    private static List<DependencyEdge> historicDependencies(Map<String,Object> values,String origin){
        Map<DependencyEdge.Phase,Map<String,EnumSet<HistoricFlag>>> phases=new EnumMap<>(DependencyEdge.Phase.class);
        for(String key:List.of("load-after","load-before","dependencies")){
            Object raw=values.get(key);if(raw==null)continue;if(!(raw instanceof List<?> declarations))throw new IllegalArgumentException("List required: "+key);
            for(Object declaration:declarations){
                Map<String,Object> flags=map(declaration,key);String target=string(flags,"name",true);
                DependencyEdge.Phase phase=bool(flags,"bootstrap",false)?DependencyEdge.Phase.BOOTSTRAP:DependencyEdge.Phase.SERVER;
                EnumSet<HistoricFlag> combined=phases.computeIfAbsent(phase,p->new LinkedHashMap<>()).computeIfAbsent(target,t->EnumSet.noneOf(HistoricFlag.class));
                if(key.equals("load-after"))combined.add(HistoricFlag.BEFORE);
                else if(key.equals("load-before"))combined.add(HistoricFlag.AFTER);
                else {combined.add(HistoricFlag.DEPENDENCY);if(bool(flags,"required",false))combined.add(HistoricFlag.REQUIRED);}
            }
        }
        List<DependencyEdge> result=new ArrayList<>();phases.forEach((phase,targets)->targets.forEach((target,flags)->result.add(new DependencyEdge(target,phase,flags.contains(HistoricFlag.REQUIRED),
                flags.contains(HistoricFlag.AFTER)?DependencyEdge.Ordering.AFTER_CONSUMER:flags.contains(HistoricFlag.BEFORE)?DependencyEdge.Ordering.BEFORE_CONSUMER:DependencyEdge.Ordering.NONE,
                flags.contains(HistoricFlag.DEPENDENCY),origin,Optional.empty()))));return List.copyOf(result);
    }
    private PluginDescriptor legacy(Map<String,Object> values,String text,String origin,int index)throws InvalidDescriptionException{
        PluginDescriptionFile upstream=new PluginDescriptionFile(new StringReader(text));List<DependencyEdge> edges=new ArrayList<>();
        for(String target:upstream.getDepend())edges.add(edge(target,true,DependencyEdge.Ordering.BEFORE_CONSUMER,origin));
        for(String target:upstream.getSoftDepend())edges.add(edge(target,false,DependencyEdge.Ordering.BEFORE_CONSUMER,origin));
        for(String target:upstream.getLoadBefore())edges.add(edge(target,false,DependencyEdge.Ordering.AFTER_CONSUMER,origin));
        return new PluginDescriptor(new PluginIdentity(upstream.getName(),upstream.getVersion()),PluginDescriptor.Kind.LEGACY,upstream.getMain(),origin,Optional.empty(),Optional.empty(),PluginDescriptor.EnablePhase.valueOf(upstream.getLoad().name()),upstream.getProvides(),edges,properties(values),index);
    }
    private static DependencyEdge edge(String target,boolean required,DependencyEdge.Ordering order,String origin){return new DependencyEdge(target,DependencyEdge.Phase.SERVER,required,order,order==DependencyEdge.Ordering.BEFORE_CONSUMER,origin,Optional.empty());}
    public DependencyRules dependencyRules(){
        return new DependencyRules(DependencyRules.CyclePolicy.REPORT_AND_BREAK_ORDER,(plugin,plugins)->{
            if(plugin.kind()!=PluginDescriptor.Kind.LEGACY)return plugin.dependencies();
            Set<String> before=new HashSet<>();plugin.dependencies().stream().filter(e->e.ordering()==DependencyEdge.Ordering.AFTER_CONSUMER).forEach(e->before.add(e.target()));
            List<DependencyEdge> result=new ArrayList<>();
            for(DependencyEdge edge:plugin.dependencies()){
                if(edge.ordering()!=DependencyEdge.Ordering.BEFORE_CONSUMER){result.add(edge);continue;}
                if(before.contains(edge.target()))continue;
                boolean reciprocal=plugins.stream().filter(p->p.identity().id().equals(edge.target())||p.provides().contains(edge.target())).anyMatch(p->p.dependencies().stream().anyMatch(e->e.target().equals(plugin.identity().id())&&e.ordering()==DependencyEdge.Ordering.BEFORE_CONSUMER));
                if(!reciprocal)result.add(edge);
            }
            return List.copyOf(result);
        },String::equals);
    }
    private static IllegalStateException failure(Path source,String origin,int line,String message,Throwable cause){return new IllegalStateException("INVALID_DESCRIPTOR: "+source+"!"+origin+":"+line+": "+message,cause);}
    private static PluginDescriptor.EnablePhase enablePhase(String value){return value==null?PluginDescriptor.EnablePhase.POSTWORLD:PluginDescriptor.EnablePhase.valueOf(value.toUpperCase(Locale.ROOT));}
    private static String className(String value){if(!value.matches("[A-Za-z_$][A-Za-z0-9_$]*(\\.[A-Za-z_$][A-Za-z0-9_$]*)+")||value.startsWith("java.")||value.startsWith("org.bukkit."))throw new IllegalArgumentException("Invalid plugin class namespace: "+value);return value;}
    private static Optional<String> optionalClass(Map<String,Object> values,String key){String value=string(values,key,false);return value==null?Optional.empty():Optional.of(className(value));}
    private static String string(Map<String,Object> map,String key,boolean required){Object value=map.get(key);if(value==null){if(required)throw new IllegalArgumentException("Missing "+key);return null;}if(value instanceof Map<?,?>||value instanceof List<?>||value.toString().isBlank())throw new IllegalArgumentException("Invalid scalar "+key);return value.toString();}
    private static boolean bool(Map<String,Object> map,String key,boolean fallback){Object value=map.get(key);if(value==null)return fallback;if(value instanceof Boolean result)return result;throw new IllegalArgumentException("Invalid boolean "+key);}
    private static List<String> strings(Map<String,Object> map,String key){Object value=map.get(key);if(value==null)return List.of();if(!(value instanceof List<?> list))throw new IllegalArgumentException("List required: "+key);List<String> result=new ArrayList<>();for(Object item:list){if(!(item instanceof String s)||s.isBlank())throw new IllegalArgumentException("String required in "+key);result.add(s);}return List.copyOf(result);}
    private static Map<String,Object> map(Object value,String field){if(!(value instanceof Map<?,?> raw))throw new IllegalArgumentException("Mapping required: "+field);Map<String,Object> result=new LinkedHashMap<>();for(var entry:raw.entrySet()){if(!(entry.getKey() instanceof String key))throw new IllegalArgumentException("String key required: "+field);result.put(key,entry.getValue());}return result;}
    private static Map<String,String> properties(Map<String,Object> values){Map<String,String> result=new LinkedHashMap<>();values.forEach((key,value)->result.put(key,JSON.toJson(value)));return Collections.unmodifiableMap(result);}
    private static void validateData(Object value,Set<Object> ancestors,int[] remaining){
        if(--remaining[0]<0)throw new IllegalArgumentException("Descriptor expansion limit exceeded");
        if(!(value instanceof Map<?,?>)&&!(value instanceof Collection<?>))return;
        if(!ancestors.add(value))throw new IllegalArgumentException("RECURSIVE_DESCRIPTOR_DATA");
        try{
            if(value instanceof Map<?,?> map)for(var entry:map.entrySet()){validateData(entry.getKey(),ancestors,remaining);validateData(entry.getValue(),ancestors,remaining);}
            else for(Object item:(Collection<?>)value)validateData(item,ancestors,remaining);
        }finally{ancestors.remove(value);}
    }
}
