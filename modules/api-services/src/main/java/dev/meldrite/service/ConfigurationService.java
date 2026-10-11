package dev.meldrite.service;

import dev.meldrite.plugin.spi.PluginScope;
import java.nio.file.*;
import java.io.IOException;
import java.util.*;

/** Neutral document/default tree and explicit owned serialization codecs. */
public final class ConfigurationService implements AutoCloseable {
    public interface Codec<T>{Map<String,Object> encode(T value);T decode(Map<String,Object> value);}
    public record Serialized(String alias,Map<String,Object> data){
        public Serialized{Objects.requireNonNull(alias);data=Collections.unmodifiableMap(new LinkedHashMap<>(data));}
    }
    private record Registered<T>(String alias,Class<T> type,PluginScope owner,Codec<T> codec){
        Map<String,Object> encode(Object value){return codec.encode(type.cast(value));}
        Object decode(Map<String,Object> data){return type.cast(Objects.requireNonNull(codec.decode(data),"Serializer returned no object"));}
    }
    private final Path dataRoot;private final Map<String,Registered<?>> codecs=new LinkedHashMap<>();
    private final Set<Document> documents=Collections.newSetFromMap(new IdentityHashMap<>());private boolean closed;
    public ConfigurationService(Path dataRoot)throws IOException{Files.createDirectories(dataRoot);if(Files.isSymbolicLink(dataRoot))throw new IllegalArgumentException("SYMLINK_DATA_ROOT");this.dataRoot=dataRoot.toRealPath();}
    private synchronized void open(){if(closed)throw new IllegalStateException("CONFIGURATION_SERVICE_CLOSED");}
    public <T> AutoCloseable registerCodec(String alias,Class<T> type,PluginScope owner,Codec<T> codec){
        Objects.requireNonNull(alias);Objects.requireNonNull(type);Objects.requireNonNull(owner);Objects.requireNonNull(codec);if(alias.isBlank())throw new IllegalArgumentException("Empty serialization alias");
        Registered<T> entry=new Registered<>(alias,type,owner,codec);
        synchronized(this){open();if(owner.isClosed())throw new IllegalStateException("OWNER_DISABLED");if(codecs.containsKey(alias)||codecs.values().stream().anyMatch(value->value.type==type))throw new IllegalArgumentException("SERIALIZATION_ALIAS_ALREADY_REGISTERED: "+alias);codecs.put(alias,entry);}
        return owner.register(()->{synchronized(ConfigurationService.this){codecs.remove(alias,entry);}});
    }
    private synchronized Registered<?> codec(String alias){open();var value=codecs.get(alias);if(value==null||value.owner.isClosed())throw new IllegalArgumentException("UNRESOLVED_SERIALIZATION_ALIAS: "+alias);return value;}
    public Object encode(Object value){return encode(value,new IdentityHashMap<>(),0);}
    private Object encode(Object value,IdentityHashMap<Object,Boolean> path,int depth){
        open();if(depth>128)throw new IllegalArgumentException("CONFIGURATION_TOO_DEEP");
        if(value==null||value instanceof String||value instanceof Boolean||value instanceof Byte||value instanceof Short||value instanceof Integer||value instanceof Long||value instanceof Float||value instanceof Double||value instanceof Character)return value;
        if(path.put(value,true)!=null)throw new IllegalArgumentException("CYCLIC_CONFIGURATION_VALUE");
        try{
            if(value instanceof Serialized record)return new Serialized(record.alias(),tree(record.data(),path,depth+1));
            if(value instanceof Map<?,?> map)return tree(map,path,depth+1);
            if(value instanceof List<?> list){var result=new ArrayList<>();for(Object item:list)result.add(encode(item,path,depth+1));return Collections.unmodifiableList(result);}
            List<Registered<?>> matches;synchronized(this){matches=codecs.values().stream().filter(entry->!entry.owner.isClosed()&&entry.type.isInstance(value)).toList();}
            if(matches.size()!=1)throw new IllegalArgumentException("UNREGISTERED_OR_AMBIGUOUS_CONFIGURATION_TYPE: "+value.getClass().getName());
            var registered=matches.get(0);return new Serialized(registered.alias,tree(Objects.requireNonNull(registered.encode(value)),path,depth+1));
        }finally{path.remove(value);}
    }
    private Map<String,Object> tree(Map<?,?> input,IdentityHashMap<Object,Boolean> path,int depth){
        var result=new LinkedHashMap<String,Object>();for(var entry:input.entrySet()){if(!(entry.getKey() instanceof String key))throw new IllegalArgumentException("CONFIGURATION_KEY_MUST_BE_STRING");result.put(key,encode(entry.getValue(),path,depth));}return Collections.unmodifiableMap(result);
    }
    public Object decode(Object value){
        open();if(value instanceof Serialized record){var registered=codec(record.alias());return registered.decode(decodedMap(record.data()));}
        if(value instanceof Map<?,?> map)return decodedMap(map);
        if(value instanceof List<?> list){var result=new ArrayList<>();for(Object item:list)result.add(decode(item));return Collections.unmodifiableList(result);}return value;
    }
    private Map<String,Object> decodedMap(Map<?,?> map){var result=new LinkedHashMap<String,Object>();for(var entry:map.entrySet())result.put((String)entry.getKey(),decode(entry.getValue()));return Collections.unmodifiableMap(result);}
    public synchronized Document document(PluginScope owner){open();Objects.requireNonNull(owner);if(owner.isClosed())throw new IllegalStateException("OWNER_DISABLED");var document=new Document(owner);documents.add(document);return owner.register(document);}
    public final class Document implements AutoCloseable {
        private final PluginScope owner;private Map<String,Object> values=Map.of(),defaults=Map.of();private volatile boolean closed;private boolean hasDefaults;
        private Document(PluginScope owner){this.owner=owner;}
        private void live(){open();if(closed||owner.isClosed())throw new IllegalStateException("CONFIGURATION_DOCUMENT_CLOSED");}
        public Object encodeValue(Object value){live();return encode(value);}
        public Object decodeValue(Object value){live();return decode(value);}
        public boolean isClosed(){return closed;}
        public synchronized boolean hasDefaults(){live();return hasDefaults;}
        public synchronized Optional<Object> lookup(List<String> path,boolean defaultTree){live();return Optional.ofNullable(at(defaultTree?defaults:values,path));}
        public synchronized Map<String,Object> snapshot(boolean defaultTree){live();return defaultTree?defaults:values;}
        public void set(List<String> path,Object value,boolean defaultTree){
            if(path.isEmpty()||path.stream().anyMatch(String::isEmpty))throw new IllegalArgumentException("Empty configuration path");Object encoded=encodeValue(value);
            synchronized(this){live();if(defaultTree){defaults=write(defaults,path,0,encoded);hasDefaults=true;}else values=write(values,path,0,encoded);}
        }
        public void replace(Map<String,Object> candidate,boolean defaultTree){
            @SuppressWarnings("unchecked") var encoded=(Map<String,Object>)encodeValue(candidate);
            // Invoke/validate every decoder before publication, with no document lock held.
            decodeValue(encoded);
            synchronized(this){live();if(defaultTree){defaults=encoded;hasDefaults=true;}else values=encoded;}
        }
        public void replacePaths(Map<String,Object> candidate,char separator){replace(normalizePaths(candidate,separator),false);}
        @Override public void close(){synchronized(this){if(closed)return;closed=true;values=Map.of();defaults=Map.of();}synchronized(ConfigurationService.this){documents.remove(this);}}
    }
    @SuppressWarnings("unchecked") private static Map<String,Object> normalizePaths(Map<String,Object> tree,char separator){
        Map<String,Object> result=Map.of();for(var entry:tree.entrySet()){
            List<String> path=Arrays.asList(entry.getKey().split(java.util.regex.Pattern.quote(String.valueOf(separator)),-1));
            if(path.stream().anyMatch(String::isEmpty))throw new IllegalArgumentException("Empty configuration path");
            Object value=entry.getValue();if(value instanceof Map<?,?> map)value=normalizePaths((Map<String,Object>)map,separator);
            result=write(result,path,0,value);
        }return result;
    }
    private static Object at(Map<String,Object> root,List<String> path){Object value=root;for(String segment:path){if(!(value instanceof Map<?,?> map))return null;value=map.get(segment);}return value;}
    @SuppressWarnings("unchecked") private static Map<String,Object> write(Map<String,Object> root,List<String> path,int index,Object value){
        var next=new LinkedHashMap<>(root);String key=path.get(index);if(index==path.size()-1){if(value==null)next.remove(key);else next.put(key,value);}else{Object existing=next.get(key);next.put(key,write(existing instanceof Map<?,?> map?(Map<String,Object>)map:Map.of(),path,index+1,value));}return Collections.unmodifiableMap(next);
    }
    public Path resolveFile(String relative)throws IOException{
        open();Objects.requireNonNull(relative);Path input=Path.of(relative);if(input.isAbsolute()||relative.isBlank())throw new IllegalArgumentException("DATA_PATH_ESCAPE");for(Path part:input)if(part.toString().equals(".."))throw new IllegalArgumentException("DATA_PATH_ESCAPE");
        Path result=dataRoot.resolve(input).normalize();if(!result.startsWith(dataRoot)||result.equals(dataRoot))throw new IllegalArgumentException("DATA_PATH_ESCAPE");Path current=dataRoot;for(Path part:dataRoot.relativize(result)){current=current.resolve(part);if(Files.isSymbolicLink(current))throw new IllegalArgumentException("SYMLINK_DATA_PATH_ESCAPE");}return result;
    }
    public String relativeFile(Path file){open();Path absolute=file.toAbsolutePath().normalize();if(!absolute.startsWith(dataRoot)||absolute.equals(dataRoot))throw new IllegalArgumentException("DATA_PATH_ESCAPE");return dataRoot.relativize(absolute).toString();}
    public void save(String relative,byte[] bytes)throws IOException{
        Objects.requireNonNull(bytes);Path target=resolveFile(relative);Files.createDirectories(target.getParent());resolveFile(relative);Path temporary=Files.createTempFile(target.getParent(),".meldrite-config-",".tmp");
        try{Files.write(temporary,bytes);open();resolveFile(relative);Files.move(temporary,target,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}finally{Files.deleteIfExists(temporary);}
    }
    @Override public void close(){List<Document> active;synchronized(this){if(closed)return;closed=true;active=List.copyOf(documents);codecs.clear();}for(var document:active)document.close();}
}
