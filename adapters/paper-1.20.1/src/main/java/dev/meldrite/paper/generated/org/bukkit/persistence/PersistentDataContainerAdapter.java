package dev.meldrite.paper.generated.org.bukkit.persistence;
import dev.meldrite.service.PersistentDataService;
import dev.meldrite.service.PersistentDataService.Kind;
import dev.meldrite.foundation.ResourceKey;
import dev.meldrite.plugin.classloading.PluginLoaderEngine;
import java.util.*;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.*;
public final class PersistentDataContainerAdapter implements PersistentDataContainer {
    private final PersistentDataService.Container container;private final PluginLoaderEngine engine;private final PersistentDataAdapterContext context;
    public PersistentDataContainerAdapter(PersistentDataService.Container container,PluginLoaderEngine engine){this.container=Objects.requireNonNull(container);this.engine=Objects.requireNonNull(engine);this.context=new PersistentDataAdapterContextAdapter(container.service(),engine);}
    private static ResourceKey key(NamespacedKey key){if(key==null)throw new IllegalArgumentException("Persistent key cannot be null");return new ResourceKey("meldrite","persistent_data",key.getNamespace(),key.getKey());}
    private static Kind kind(Class<?> type){
        if(type==Byte.class)return Kind.BYTE;if(type==Short.class)return Kind.SHORT;if(type==Integer.class)return Kind.INTEGER;if(type==Long.class)return Kind.LONG;if(type==Float.class)return Kind.FLOAT;if(type==Double.class)return Kind.DOUBLE;if(type==String.class)return Kind.STRING;
        if(type==byte[].class)return Kind.BYTES;if(type==int[].class)return Kind.INTEGERS;if(type==long[].class)return Kind.LONGS;if(type==PersistentDataContainer.class)return Kind.CONTAINER;if(type==PersistentDataContainer[].class)return Kind.CONTAINERS;
        throw new IllegalArgumentException("UNSUPPORTED_PERSISTENT_PRIMITIVE: "+type);
    }
    private Object encode(Kind kind,Object value){
        if(kind==Kind.CONTAINER){if(!(value instanceof PersistentDataContainerAdapter adapter))throw new UnsupportedOperationException("FOREIGN_PERSISTENT_CONTAINER");return adapter.container.snapshot();}
        if(kind==Kind.CONTAINERS)return Arrays.stream((PersistentDataContainer[])value).map(item->encode(Kind.CONTAINER,item)).toList();return value;
    }
    private Object decode(Kind kind,Object value){
        if(kind==Kind.CONTAINER)return new PersistentDataContainerAdapter(container.service().restore((PersistentDataService.Snapshot)value),engine);
        if(kind==Kind.CONTAINERS)return ((List<?>)value).stream().map(item->(PersistentDataContainer)decode(Kind.CONTAINER,item)).toArray(PersistentDataContainer[]::new);return value;
    }
    private <T,Z> PersistentDataTypeAdapter<T,Z> type(PersistentDataType<T,Z> type){if(type==null)throw new IllegalArgumentException("Persistent type cannot be null");return new PersistentDataTypeAdapter<>(engine,type);}
    public <T,Z> void set(NamespacedKey key,PersistentDataType<T,Z> type,Z value){ResourceKey logical=key(key);if(value==null)throw new IllegalArgumentException("Persistent value cannot be null");var converter=type(type);Kind kind=kind(converter.getPrimitiveType());Object primitive=converter.toPrimitive(value,context);container.set(logical,kind,encode(kind,primitive));}
    public <T,Z> boolean has(NamespacedKey key,PersistentDataType<T,Z> type){ResourceKey logical=key(key);Kind kind=kind(type(type).getPrimitiveType());return container.get(logical).map(value->value.kind()==kind).orElse(false);}
    public boolean has(NamespacedKey key){return container.get(key(key)).isPresent();}
    public <T,Z> Z get(NamespacedKey key,PersistentDataType<T,Z> type){ResourceKey logical=key(key);var converter=type(type);Class<T> primitive=converter.getPrimitiveType();Kind kind=kind(primitive);Object value=container.get(logical,kind);return value==null?null:converter.fromPrimitive(primitive.cast(decode(kind,value)),context);}
    public <T,Z> Z getOrDefault(NamespacedKey key,PersistentDataType<T,Z> type,Z fallback){Z value=get(key,type);return value==null?fallback:value;}
    public Set<NamespacedKey> getKeys(){var result=new LinkedHashSet<NamespacedKey>();for(ResourceKey key:container.keys()){if(!key.registryNamespace().equals("meldrite")||!key.registryPath().equals("persistent_data"))throw new UnsupportedOperationException("FOREIGN_PERSISTENT_KEY_REGISTRY: "+key);result.add(new NamespacedKey(key.entryNamespace(),key.entryPath()));}return Collections.unmodifiableSet(result);}
    public void remove(NamespacedKey key){container.remove(key(key));}
    public boolean isEmpty(){return container.isEmpty();}
    public PersistentDataAdapterContext getAdapterContext(){return context;}
    public byte[] serializeToBytes()throws java.io.IOException{throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/persistence/PersistentDataContainer#serializeToBytes()[B#METHOD native NBT codec requires V3");}
    public void readFromBytes(byte[] bytes,boolean clear)throws java.io.IOException{throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/persistence/PersistentDataContainer#readFromBytes([BZ)V#METHOD native NBT codec requires V3");}
}
