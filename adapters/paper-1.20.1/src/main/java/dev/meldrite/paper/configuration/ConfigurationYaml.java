package dev.meldrite.paper.configuration;
import dev.meldrite.service.ConfigurationService;
import java.util.*;
import org.yaml.snakeyaml.*;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.bukkit.configuration.InvalidConfigurationException;
/** Safe YAML syntax codec; registered object construction remains scope-owned. */
public final class ConfigurationYaml {
    private ConfigurationYaml(){}
    private static Yaml yaml(){var options=new LoaderOptions();options.setAllowDuplicateKeys(false);options.setMaxAliasesForCollections(50);return new Yaml(new SafeConstructor(options));}
    private static Object encode(Object value){
        if(value instanceof ConfigurationService.Serialized record){if(record.data().containsKey("=="))throw new IllegalArgumentException("RESERVED_SERIALIZABLE_FIELD");var result=new LinkedHashMap<String,Object>();result.put("==",record.alias());record.data().forEach((key,item)->result.put(key,encode(item)));return result;}
        if(value instanceof Map<?,?> map){var result=new LinkedHashMap<String,Object>();map.forEach((key,item)->result.put((String)key,encode(item)));return result;}if(value instanceof List<?> list)return list.stream().map(ConfigurationYaml::encode).toList();return value;
    }
    private static Object decode(Object value,IdentityHashMap<Object,Boolean> path,int depth){
        if(depth>128)throw new IllegalArgumentException("YAML_TOO_DEEP");if(value instanceof Map<?,?> map){if(path.put(value,true)!=null)throw new IllegalArgumentException("CYCLIC_YAML");try{var result=new LinkedHashMap<String,Object>();for(var entry:map.entrySet()){if(!(entry.getKey() instanceof String key))throw new IllegalArgumentException("YAML_KEY_MUST_BE_STRING");result.put(key,decode(entry.getValue(),path,depth+1));}if(result.containsKey("==")){Object alias=result.remove("==");if(!(alias instanceof String name))throw new IllegalArgumentException("INVALID_SERIALIZATION_ALIAS");return new ConfigurationService.Serialized(name,result);}return result;}finally{path.remove(value);}}
        if(value instanceof List<?> list){if(path.put(value,true)!=null)throw new IllegalArgumentException("CYCLIC_YAML");try{var result=new ArrayList<>();for(Object item:list)result.add(decode(item,path,depth+1));return result;}finally{path.remove(value);}}return value;
    }
    public static String save(ConfigurationService.Document document){return yaml().dump(encode(document.snapshot(false)));}
    @SuppressWarnings("unchecked") public static void load(ConfigurationService.Document document,String contents,char separator)throws InvalidConfigurationException {
        Objects.requireNonNull(contents);try{Object parsed=yaml().load(contents);Object tree=parsed==null?Map.of():decode(parsed,new IdentityHashMap<>(),0);if(!(tree instanceof Map<?,?> map))throw new IllegalArgumentException("YAML_ROOT_MUST_BE_MAP");document.replacePaths((Map<String,Object>)map,separator);}catch(org.yaml.snakeyaml.error.YAMLException|IllegalArgumentException failure){throw new InvalidConfigurationException(failure);}
    }
}
