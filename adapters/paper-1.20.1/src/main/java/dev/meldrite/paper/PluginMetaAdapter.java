package dev.meldrite.paper;
import com.google.gson.Gson;
import dev.meldrite.plugin.spi.*;
import io.papermc.paper.plugin.configuration.PluginMeta;
import java.io.StringReader;
import java.util.*;
import org.bukkit.permissions.*;
import org.bukkit.plugin.*;
import org.yaml.snakeyaml.Yaml;
/** Metadata access only; permission registration and command behavior belong to services. */
public final class PluginMetaAdapter implements PluginMeta {
    private final PluginDescriptionFile description;
    private PluginMetaAdapter(PluginDescriptionFile description){this.description=description;}
    public record Metadata(PluginDescriptionFile description,PluginMeta meta){}
    public static Metadata create(PluginDescriptor descriptor)throws InvalidDescriptionException {
        Map<String,Object> values=new LinkedHashMap<>();Gson json=new Gson();descriptor.properties().forEach((key,value)->values.put(key,json.fromJson(value,Object.class)));
        if(descriptor.kind()==PluginDescriptor.Kind.LEGACY){var description=new PluginDescriptionFile(new StringReader(new Yaml().dump(values)));return new Metadata(description,description);}
        PermissionDefault defaultPermission=values.containsKey("defaultPerm")?PermissionDefault.getByName(String.valueOf(values.get("defaultPerm"))):PermissionDefault.OP;
        if(defaultPermission==null)throw new IllegalArgumentException("Unknown permission default: "+values.get("defaultPerm"));
        List<Permission> permissions=new ArrayList<>();Object rawPermissions=values.get("permissions");
        if(rawPermissions!=null){if(!(rawPermissions instanceof Map<?,?> map))throw new IllegalArgumentException("permissions must be a map");for(var entry:map.entrySet()){if(!(entry.getValue() instanceof Map<?,?> flags))throw new IllegalArgumentException("permission flags must be a map");permissions.add(Permission.loadPermission(entry.getKey().toString(),flags,defaultPermission,permissions));}}
        List<String> authors=new ArrayList<>(list(values,"authors"));if(values.containsKey("author"))authors.add(scalar(values,"author"));
        List<String> required=edges(descriptor,true,null),optional=edges(descriptor,false,null),before=edges(descriptor,null,DependencyEdge.Ordering.AFTER_CONSUMER);
        PluginDescriptionFile description=new PluginDescriptionFile(descriptor.identity().id(),descriptor.identity().id(),descriptor.provides(),descriptor.mainClass(),"",required,optional,before,descriptor.identity().version(),commands(values),scalar(values,"description"),authors,list(values,"contributors"),scalar(values,"website"),scalar(values,"prefix"),PluginLoadOrder.valueOf(descriptor.enablePhase().name()),permissions,defaultPermission,Set.of(),scalar(values,"api-version"),list(values,"libraries"));
        return new Metadata(description,new PluginMetaAdapter(description));
    }
    private static Map<String,Map<String,Object>> commands(Map<String,Object> values){
        Object raw=values.get("commands");
        if(raw==null)return Map.of();
        if(!(raw instanceof Map<?,?> map))throw new IllegalArgumentException("commands must be a map");
        Map<String,Map<String,Object>> result=new LinkedHashMap<>();
        for(var entry:map.entrySet()){
            if(!(entry.getKey() instanceof String key)||!(entry.getValue() instanceof Map<?,?> metadata))throw new IllegalArgumentException("command metadata must be a map");
            Map<String,Object> copy=new LinkedHashMap<>();
            for(var item:metadata.entrySet()){
                if(!(item.getKey() instanceof String field))throw new IllegalArgumentException("command metadata key must be a string");
                copy.put(field,item.getValue());
            }
            result.put(key,copy);
        }
        return result;
    }
    private static List<String> edges(PluginDescriptor descriptor,Boolean required,DependencyEdge.Ordering ordering){return descriptor.dependencies().stream().filter(e->e.phase()==DependencyEdge.Phase.SERVER).filter(e->ordering!=null?e.ordering()==ordering:e.joinClasspath()&&e.required()==required).map(DependencyEdge::target).distinct().toList();}
    private static String scalar(Map<String,Object> values,String key){Object value=values.get(key);if(value==null)return null;if(value instanceof Collection<?>||value instanceof Map<?,?>)throw new IllegalArgumentException("Expected scalar: "+key);return value.toString();}
    private static List<String> list(Map<String,Object> values,String key){Object value=values.get(key);if(value==null)return List.of();if(!(value instanceof List<?> list)||list.stream().anyMatch(item->!(item instanceof String)))throw new IllegalArgumentException("Expected string list: "+key);return list.stream().map(String.class::cast).toList();}
    public String getName(){return description.getName();}public String getMainClass(){return description.getMain();}public PluginLoadOrder getLoadOrder(){return description.getLoad();}public String getVersion(){return description.getVersion();}public String getLoggerPrefix(){return description.getPrefix();}
    public List<String> getPluginDependencies(){return description.getDepend();}public List<String> getPluginSoftDependencies(){return description.getSoftDepend();}public List<String> getLoadBeforePlugins(){return description.getLoadBefore();}public List<String> getProvidedPlugins(){return description.getProvides();}
    public List<String> getAuthors(){return description.getAuthors();}public List<String> getContributors(){return description.getContributors();}public String getDescription(){return description.getDescription();}public String getWebsite(){return description.getWebsite();}
    public List<Permission> getPermissions(){return description.getPermissions();}public PermissionDefault getPermissionDefault(){return description.getPermissionDefault();}public String getAPIVersion(){return description.getAPIVersion();}
}
