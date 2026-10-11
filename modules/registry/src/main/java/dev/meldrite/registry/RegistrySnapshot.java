package dev.meldrite.registry;

import dev.meldrite.foundation.*;
import java.util.*;

public record RegistrySnapshot(String scope, Lifecycle lifecycle, RegistryEpoch epoch,
                               Map<ResourceKey,Entry> entries, boolean frozen) {
    public enum Lifecycle { STATIC, WORLD, RELOADABLE }
    public enum Origin { VANILLA, MOD, DATAPACK }
    public record Reference(String scope, ResourceKey key) {
        public Reference { Objects.requireNonNull(scope); Objects.requireNonNull(key); if(scope.isBlank())throw new IllegalArgumentException("Empty scope"); }
    }
    public record Entry(ResourceKey key, Origin origin, String codecId, byte[] payload, Set<Reference> references) {
        public Entry {
            Objects.requireNonNull(key); Objects.requireNonNull(origin); Objects.requireNonNull(codecId);
            if(codecId.isBlank())throw new IllegalArgumentException("Empty codec");
            payload=Objects.requireNonNull(payload).clone(); references=Set.copyOf(references);
        }
        @Override public byte[] payload(){return payload.clone();}
        @Override public boolean equals(Object other){return other instanceof Entry e&&key.equals(e.key)&&origin==e.origin&&codecId.equals(e.codecId)&&Arrays.equals(payload,e.payload)&&references.equals(e.references);}
        @Override public int hashCode(){return Objects.hash(key,origin,codecId,Arrays.hashCode(payload),references);}
    }
    public RegistrySnapshot {
        Objects.requireNonNull(scope); Objects.requireNonNull(lifecycle); Objects.requireNonNull(epoch);
        if(scope.isBlank()||!epoch.scope().equals(scope))throw new IllegalArgumentException("Invalid scope/epoch");
        entries=Map.copyOf(entries);
        for(var entry:entries.entrySet())if(!entry.getKey().equals(entry.getValue().key()))throw new IllegalArgumentException("Entry key mismatch");
    }
    public static RegistrySnapshot candidate(String scope,Lifecycle lifecycle,Collection<Entry> entries,boolean frozen){
        Map<ResourceKey,Entry> map=new LinkedHashMap<>();
        for(Entry entry:entries)if(map.putIfAbsent(entry.key(),entry)!=null)throw new IllegalArgumentException("Duplicate entry: "+entry.key());
        return new RegistrySnapshot(scope,lifecycle,new RegistryEpoch(scope,0),map,frozen);
    }
}
