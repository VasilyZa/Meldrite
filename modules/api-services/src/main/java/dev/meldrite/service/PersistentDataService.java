package dev.meldrite.service;

import dev.meldrite.foundation.*;
import java.util.*;

/** Detached typed data; native NBT/component decoding and lossless item merging remain bridge effects. */
public final class PersistentDataService implements AutoCloseable {
    public enum Kind {BYTE,SHORT,INTEGER,LONG,FLOAT,DOUBLE,STRING,BYTES,INTEGERS,LONGS,CONTAINER,CONTAINERS,OPAQUE}
    public record Opaque(String codec,byte[] bytes){
        public Opaque{Objects.requireNonNull(codec);if(codec.isBlank())throw new IllegalArgumentException("Empty opaque codec");bytes=Objects.requireNonNull(bytes).clone();}
        @Override public byte[] bytes(){return bytes.clone();}
        @Override public boolean equals(Object value){return value instanceof Opaque other&&codec.equals(other.codec)&&Arrays.equals(bytes,other.bytes);}
        @Override public int hashCode(){return 31*codec.hashCode()+Arrays.hashCode(bytes);}
    }
    public record Value(Kind kind,Object value){
        public Value{Objects.requireNonNull(kind);value=copy(kind,Objects.requireNonNull(value));}
        @Override public Object value(){return copy(kind,value);}
        @Override public boolean equals(Object other){return other instanceof Value entry&&kind==entry.kind&&Objects.deepEquals(value,entry.value);}
        @Override public int hashCode(){return 31*kind.hashCode()+Arrays.deepHashCode(new Object[]{value});}
    }
    public record Snapshot(Map<ResourceKey,Value> entries){
        public Snapshot{var copy=new LinkedHashMap<ResourceKey,Value>();entries.forEach((key,value)->copy.put(Objects.requireNonNull(key),new Value(value.kind(),value.value())));entries=Collections.unmodifiableMap(copy);}
    }
    public record MetadataSnapshot(ItemSnapshot original,Snapshot data){public MetadataSnapshot{Objects.requireNonNull(original);Objects.requireNonNull(data);}}
    private static Object copy(Kind kind,Object value){return switch(kind){
        case BYTE->Byte.class.cast(value);case SHORT->Short.class.cast(value);case INTEGER->Integer.class.cast(value);case LONG->Long.class.cast(value);case FLOAT->Float.class.cast(value);case DOUBLE->Double.class.cast(value);case STRING->String.class.cast(value);
        case BYTES->byte[].class.cast(value).clone();case INTEGERS->int[].class.cast(value).clone();case LONGS->long[].class.cast(value).clone();
        case CONTAINER->new Snapshot(Snapshot.class.cast(value).entries());
        case CONTAINERS->{if(!(value instanceof List<?> list))throw new IllegalArgumentException("Persistent container array must be a list of snapshots");yield list.stream().map(item->new Snapshot(Snapshot.class.cast(item).entries())).toList();}
        case OPAQUE->{var opaque=Opaque.class.cast(value);yield new Opaque(opaque.codec(),opaque.bytes());}
    };}
    private final Set<Container> containers=Collections.newSetFromMap(new WeakHashMap<>());private boolean closed;
    private synchronized void open(){if(closed)throw new IllegalStateException("PERSISTENT_DATA_SERVICE_CLOSED");}
    public synchronized Container container(){open();var result=new Container(Map.of());containers.add(result);return result;}
    public synchronized Container restore(Snapshot snapshot){open();Objects.requireNonNull(snapshot);var result=new Container(snapshot.entries());containers.add(result);return result;}
    public Metadata metadata(ItemSnapshot original){open();return new Metadata(original,container());}
    public Metadata restore(MetadataSnapshot snapshot){open();Objects.requireNonNull(snapshot);return new Metadata(snapshot.original(),restore(snapshot.data()));}
    public final class Container {
        private final Map<ResourceKey,Value> entries=new LinkedHashMap<>();
        private Container(Map<ResourceKey,Value> values){values.forEach((key,value)->entries.put(key,new Value(value.kind(),value.value())));}
        public PersistentDataService service(){return PersistentDataService.this;}
        public synchronized void set(ResourceKey key,Kind kind,Object value){open();Objects.requireNonNull(key);Value prepared=new Value(kind,value);entries.put(key,prepared);}
        public synchronized Optional<Value> get(ResourceKey key){open();return Optional.ofNullable(entries.get(Objects.requireNonNull(key)));}
        public synchronized Object get(ResourceKey key,Kind kind){open();Objects.requireNonNull(kind);Value value=entries.get(Objects.requireNonNull(key));if(value==null)return null;if(value.kind()!=kind)throw new IllegalArgumentException("PERSISTENT_PRIMITIVE_TYPE_MISMATCH: "+key);return value.value();}
        public synchronized Set<ResourceKey> keys(){open();return Collections.unmodifiableSet(new LinkedHashSet<>(entries.keySet()));}
        public synchronized void remove(ResourceKey key){open();entries.remove(Objects.requireNonNull(key));}
        public synchronized boolean isEmpty(){open();return entries.isEmpty();}
        public synchronized Snapshot snapshot(){open();return new Snapshot(entries);}
        public Container copy(){return restore(snapshot());}
        private synchronized void clearClosed(){entries.clear();}
    }
    public final class Metadata {
        private final ItemSnapshot original;private final Container data;
        private Metadata(ItemSnapshot original,Container data){this.original=Objects.requireNonNull(original);this.data=data;}
        public ItemSnapshot original(){return original;}
        public Container persistentData(){open();return data;}
        public MetadataSnapshot snapshot(){open();return new MetadataSnapshot(original,data.snapshot());}
        public Metadata copy(){return restore(snapshot());}
    }
    @Override public void close(){List<Container> active;synchronized(this){if(closed)return;closed=true;active=List.copyOf(containers);containers.clear();}for(var container:active)container.clearClosed();}
}
