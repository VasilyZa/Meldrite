package dev.meldrite.registry;

import dev.meldrite.foundation.*;
import java.util.*;

/** A single immutable publication contains all registry groups; readers never see half a reload. */
public final class RegistryCatalog {
    record State(long publication,Map<String,RegistrySnapshot> snapshots) {
        State { snapshots=Map.copyOf(snapshots); }
    }
    private volatile State state=new State(0,Map.of());
    State state(){return state;}
    synchronized void publish(State expected,Map<String,RegistrySnapshot> snapshots){
        if(state!=expected)throw new IllegalStateException("STALE_RELOAD_PREPARATION");
        state=new State(Math.addExact(expected.publication(),1),snapshots);
    }
    public Optional<RegistrySnapshot> snapshot(String scope){return Optional.ofNullable(state.snapshots().get(scope));}
    public Map<String,RegistrySnapshot> snapshots(){return state.snapshots();}
    public ProjectionResult<RegistrySnapshot.Entry> resolve(ResourceKey key,String scope,RegistryEpoch epoch){
        RegistrySnapshot snapshot=state.snapshots().get(scope);
        if(snapshot==null)return new ProjectionResult.Unavailable<>("SCOPE_UNAVAILABLE: "+scope);
        if(!snapshot.epoch().equals(epoch))return new ProjectionResult.Unavailable<>("RERESOLVE");
        RegistrySnapshot.Entry entry=snapshot.entries().get(key);
        return entry==null?new ProjectionResult.Unavailable<>("ENTRY_UNAVAILABLE: "+key):new ProjectionResult.Exact<>(entry);
    }
}
