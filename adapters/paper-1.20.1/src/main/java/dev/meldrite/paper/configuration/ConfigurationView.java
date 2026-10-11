package dev.meldrite.paper.configuration;
import dev.meldrite.service.ConfigurationService;
import dev.meldrite.paper.generated.org.bukkit.configuration.*;
import java.util.*;
import org.bukkit.configuration.*;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
/** Profile types/primitive conversions; every tree operation uses the one neutral document. */
public final class ConfigurationView {
    private final ConfigurationService.Document document;private final Configuration root;private final List<String> prefix;private final boolean defaults;
    private final Map<List<String>,ConfigurationSection> sections;
    private Configuration defaultRoot;
    public ConfigurationView(ConfigurationService.Document document,Configuration root,List<String> prefix,boolean defaults){this(document,root,prefix,defaults,new HashMap<>());}
    private ConfigurationView(ConfigurationService.Document document,Configuration root,List<String> prefix,boolean defaults,Map<List<String>,ConfigurationSection> sections){this.document=Objects.requireNonNull(document);this.root=Objects.requireNonNull(root);this.prefix=List.copyOf(prefix);this.defaults=defaults;this.sections=sections;}
    private List<String> path(String path){Objects.requireNonNull(path);var result=new ArrayList<>(prefix);if(!path.isEmpty())result.addAll(Arrays.asList(path.split(java.util.regex.Pattern.quote(String.valueOf(root.options().pathSeparator())),-1)));return List.copyOf(result);}
    private String join(List<String> path){return String.join(String.valueOf(root.options().pathSeparator()),path);}
    private Object actual(List<String> path){return document.lookup(path,defaults).orElse(null);}
    private Object fallback(List<String> path){return defaults?null:document.lookup(path,true).orElse(null);}
    private ConfigurationSection section(List<String> path){if(path.isEmpty())return root;return sections.computeIfAbsent(path,key->new ConfigurationSectionAdapter(new ConfigurationView(document,root,key,defaults,sections)));}
    private Object api(Object value,List<String> path){return value instanceof Map<?,?>?section(path):document.decodeValue(value);}
    public Object get(String path){var key=path(path);Object value=actual(key);return api(value==null?fallback(key):value,key);}
    public Object get(String path,Object fallback){var key=path(path);Object value=actual(key);return value==null?fallback:api(value,key);}
    public boolean contains(String path){return get(path)!=null;}public boolean contains(String path,boolean ignoreDefaults){return ignoreDefaults?get(path,null)!=null:contains(path);}
    public boolean isSet(String path){return contains(path,!root.options().copyDefaults());}
    private Object input(Object value){return input(value,0);}
    private Object input(Object value,int depth){if(depth>128)throw new IllegalArgumentException("CONFIGURATION_TOO_DEEP");if(value instanceof ConfigurationSection section)return input(section.getValues(false),depth+1);if(value instanceof Map<?,?> map){var values=new LinkedHashMap<String,Object>();for(var entry:map.entrySet()){if(!(entry.getKey() instanceof String key))throw new IllegalArgumentException("Configuration key must be a string");values.put(key,input(entry.getValue(),depth+1));}return values;}if(value instanceof List<?> list){var values=new ArrayList<>();for(Object item:list)values.add(input(item,depth+1));return values;}return value;}
    public void set(String path,Object value){document.set(path(path),input(value),defaults);}
    public ConfigurationSection createSection(String path){set(path,Map.of());return section(path(path));}
    public ConfigurationSection createSection(String path,Map<?,?> values){set(path,input(values));return section(path(path));}
    public ConfigurationSection getConfigurationSection(String path){return get(path) instanceof ConfigurationSection value?value:null;}
    public boolean isConfigurationSection(String path){return get(path) instanceof ConfigurationSection;}
    public Configuration getRoot(){return root;}public String getCurrentPath(){return join(prefix);}public String getName(){return prefix.isEmpty()?"":prefix.get(prefix.size()-1);}
    public ConfigurationSection getParent(){return prefix.isEmpty()?null:section(prefix.subList(0,prefix.size()-1));}
    public void addDefault(String path,Object value){document.set(path(path),input(value),true);}
    public void addDefaults(Map<String,Object> values){for(var entry:values.entrySet())addDefault(entry.getKey(),entry.getValue());}
    public void addDefaults(Configuration values){addDefaults(values.getValues(false));}
    @SuppressWarnings("unchecked") public void setDefaults(Configuration values){document.replace((Map<String,Object>)input(Objects.requireNonNull(values).getValues(false)),true);}
    public Configuration getDefaults(){if(defaults||!document.hasDefaults())return null;if(defaultRoot==null)defaultRoot=new ConfigurationAdapter(document,true);return defaultRoot;}
    public ConfigurationSection getDefaultSection(){var defaultRoot=getDefaults();return defaultRoot==null?null:defaultRoot.getConfigurationSection(join(prefix));}
    private void keys(Map<?,?> values,List<String> path,boolean deep,Set<String> result){for(var entry:values.entrySet()){var child=new ArrayList<>(path);child.add((String)entry.getKey());result.add(join(child));if(deep&&entry.getValue() instanceof Map<?,?> nested)keys(nested,child,true,result);}}
    public Set<String> getKeys(boolean deep){var result=new LinkedHashSet<String>();var key=path("");if(!defaults&&root.options().copyDefaults()&&fallback(key) instanceof Map<?,?> map)keys(map,List.of(),deep,result);if(actual(key) instanceof Map<?,?> map)keys(map,List.of(),deep,result);return Collections.unmodifiableSet(result);}
    public Map<String,Object> getValues(boolean deep){var result=new LinkedHashMap<String,Object>();for(String key:getKeys(deep))result.put(key,get(key));return Collections.unmodifiableMap(result);}
    public String getString(String path){Object value=fallback(path(path));return getString(path,value==null?null:value.toString());}
    public String getString(String path,String fallback){Object value=get(path,fallback);return value==null?null:value.toString();}public boolean isString(String path){return get(path) instanceof String;}
    public boolean getBoolean(String path){Object value=fallback(path(path));return getBoolean(path,value instanceof Boolean bool&&bool);}
    public boolean getBoolean(String path,boolean fallback){Object value=get(path,fallback);return value instanceof Boolean bool?bool:fallback;}public boolean isBoolean(String path){return get(path) instanceof Boolean;}
    public int getInt(String path){Object value=fallback(path(path));return getInt(path,value instanceof Number number?number.intValue():0);}
    public int getInt(String path,int fallback){Object value=get(path,fallback);return value instanceof Number number?number.intValue():fallback;}public boolean isInt(String path){return get(path) instanceof Integer;}
    public long getLong(String path){Object value=fallback(path(path));return getLong(path,value instanceof Number number?number.longValue():0);}
    public long getLong(String path,long fallback){Object value=get(path,fallback);return value instanceof Number number?number.longValue():fallback;}public boolean isLong(String path){return get(path) instanceof Long;}
    public double getDouble(String path){Object value=fallback(path(path));return getDouble(path,value instanceof Number number?number.doubleValue():0);}
    public double getDouble(String path,double fallback){Object value=get(path,fallback);return value instanceof Number number?number.doubleValue():fallback;}public boolean isDouble(String path){return get(path) instanceof Double;}
    public List<?> getList(String path){Object value=fallback(path(path));return getList(path,value instanceof List<?> list?list:null);}
    public List<?> getList(String path,List<?> fallback){Object value=get(path,fallback);return value instanceof List<?> list?list:fallback;}public boolean isList(String path){return get(path) instanceof List;}
    public <T> T getObject(String path,Class<T> type){Object value=document.decodeValue(fallback(path(path)));return getObject(path,type,type.isInstance(value)?type.cast(value):null);}
    public <T> T getObject(String path,Class<T> type,T fallback){Object value=get(path,fallback);return type.isInstance(value)?type.cast(value):fallback;}
    public <T extends ConfigurationSerializable> T getSerializable(String path,Class<T> type){return getObject(path,type);}
    public <T extends ConfigurationSerializable> T getSerializable(String path,Class<T> type,T fallback){return getObject(path,type,fallback);}
}
