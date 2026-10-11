package dev.meldrite.service;
import dev.meldrite.bridge.*;
import dev.meldrite.foundation.WorldKey;
import java.util.*;
public final class WorldService {
    public record Reference(WorldKey key,long generation,UUID uid,String name) {public Reference{Objects.requireNonNull(key);Objects.requireNonNull(uid);Objects.requireNonNull(name);if(generation<0||name.isBlank())throw new IllegalArgumentException("Invalid world identity");}}
    private final WorldPort worlds;
    private final ExecutionPort execution;
    private final Map<WorldKey,Reference> active=new LinkedHashMap<>();
    private long generation;
    public WorldService(WorldPort worlds,ExecutionPort execution){this.worlds=Objects.requireNonNull(worlds);this.execution=Objects.requireNonNull(execution);}
    public synchronized Reference bind(WorldKey key,UUID uid,String name){requirePrimary();if(active.containsKey(key))throw new IllegalStateException("WORLD_ALREADY_BOUND: "+key);Reference reference=new Reference(key,++generation,uid,name);active.put(key,reference);return reference;}
    public synchronized boolean valid(Reference reference){return reference.equals(active.get(reference.key()))&&worlds.isLoaded(reference.key());}
    public synchronized Reference reference(WorldKey key){Reference reference=active.get(key);if(reference==null)throw new IllegalStateException("STALE_HANDLE: "+key);return reference;}
    public synchronized List<Reference> worlds(){return List.copyOf(active.values());}
    public synchronized void unbind(Reference reference){requirePrimary();active.remove(reference.key(),reference);}
    public void requireWritable(Reference reference){requirePrimary();if(!valid(reference))throw new IllegalStateException("STALE_HANDLE: "+reference.key());}
    public void save(Reference reference){requireWritable(reference);worlds.save(reference.key());}
    private void requireReadable(Reference reference){if(!valid(reference))throw new IllegalStateException("STALE_HANDLE: "+reference.key());}
    public int minHeight(Reference reference){requireReadable(reference);return worlds.minHeight(reference.key());}
    public int maxHeight(Reference reference){requireReadable(reference);return worlds.maxHeight(reference.key());}
    public String environment(Reference reference){requireReadable(reference);return worlds.environment(reference.key());}
    public dev.meldrite.foundation.ResourceKey blockType(Reference reference,dev.meldrite.foundation.BlockPosition position){
        if(!execution.isPrimaryThread())throw new IllegalStateException("ASYNC_WORLD_READ");
        if(!reference.key().equals(position.world())||!valid(reference))throw new IllegalStateException("STALE_HANDLE: "+reference.key());
        return Objects.requireNonNull(worlds.blockType(position),"Native block type returned null");
    }
    public void requirePrimary(){if(!execution.isPrimaryThread())throw new IllegalStateException("ASYNC_WORLD_WRITE");}
}
