package dev.meldrite.registry;

import dev.meldrite.bridge.ExecutionPort;
import java.util.*;
import java.util.function.Predicate;

public final class ReloadCoordinator {
    public static final class Prepared {
        final ReloadCoordinator owner; final RegistryCatalog.State base;
        final Map<String,RegistrySnapshot> replacement;
        Prepared(ReloadCoordinator owner,RegistryCatalog.State base,Map<String,RegistrySnapshot> replacement){this.owner=owner;this.base=base;this.replacement=Map.copyOf(replacement);}
    }
    private final RegistryCatalog catalog; private final ExecutionPort execution;
    private final Predicate<RegistrySnapshot.Entry> codecValidator;
    public ReloadCoordinator(RegistryCatalog catalog,ExecutionPort execution,Predicate<RegistrySnapshot.Entry> codecValidator){this.catalog=Objects.requireNonNull(catalog);this.execution=Objects.requireNonNull(execution);this.codecValidator=Objects.requireNonNull(codecValidator);}
    public Prepared prepare(Collection<RegistrySnapshot> candidates){
        RegistryCatalog.State base=catalog.state();
        Map<String,RegistrySnapshot> replacement=new LinkedHashMap<>(base.snapshots());
        Set<String> scopes=new HashSet<>();
        for(RegistrySnapshot candidate:candidates){
            if(!scopes.add(candidate.scope()))throw new IllegalArgumentException("Duplicate candidate scope: "+candidate.scope());
            RegistrySnapshot previous=base.snapshots().get(candidate.scope());
            if(previous!=null&&previous.lifecycle()!=candidate.lifecycle())throw new IllegalArgumentException("Registry lifecycle changed: "+candidate.scope());
            boolean same=previous!=null&&previous.entries().equals(candidate.entries())&&previous.frozen()==candidate.frozen();
            if(previous!=null&&previous.frozen()&&!same)throw new IllegalArgumentException("STATIC_REGISTRY_FROZEN: "+candidate.scope());
            long revision=previous==null?1:same?previous.epoch().revision():Math.addExact(previous.epoch().revision(),1);
            replacement.put(candidate.scope(),new RegistrySnapshot(candidate.scope(),candidate.lifecycle(),new dev.meldrite.foundation.RegistryEpoch(candidate.scope(),revision),candidate.entries(),candidate.frozen()));
        }
        // Validate the complete resulting graph, including old entries referencing a replaced scope.
        for(RegistrySnapshot snapshot:replacement.values())for(RegistrySnapshot.Entry entry:snapshot.entries().values()){
            if(!codecValidator.test(entry))throw new IllegalArgumentException("INVALID_REGISTRY_CODEC: "+entry.key());
            for(RegistrySnapshot.Reference reference:entry.references()){
                RegistrySnapshot target=replacement.get(reference.scope());
                if(target==null||!target.entries().containsKey(reference.key()))throw new IllegalArgumentException("DANGLING_REGISTRY_REFERENCE: "+reference);
            }
        }
        return new Prepared(this,base,replacement);
    }
    public void commit(Prepared prepared){
        if(!execution.isPrimaryThread())throw new IllegalStateException("ASYNC_REGISTRY_COMMIT");
        if(prepared.owner!=this)throw new IllegalArgumentException("Foreign preparation");
        catalog.publish(prepared.base,prepared.replacement);
    }
}
