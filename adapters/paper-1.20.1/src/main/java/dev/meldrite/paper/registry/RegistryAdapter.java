package dev.meldrite.paper.registry;
import dev.meldrite.foundation.*;
import dev.meldrite.registry.*;
import java.util.*;
import java.util.function.Function;
import org.bukkit.NamespacedKey;
/** An open keyed projection is separate from each closed enum projection. */
public final class RegistryAdapter<T> {
    private final RegistryCatalog catalog;private final Function<RegistrySnapshot.Entry,ProjectionResult<T>> projection;
    public RegistryAdapter(RegistryCatalog catalog,Function<RegistrySnapshot.Entry,ProjectionResult<T>> projection){this.catalog=Objects.requireNonNull(catalog);this.projection=Objects.requireNonNull(projection);}
    public ProjectionResult<T> resolve(ResourceKey key,String scope,RegistryEpoch epoch){
        var result=catalog.resolve(key,scope,epoch);
        if(result instanceof ProjectionResult.Exact<RegistrySnapshot.Entry> exact)return Objects.requireNonNull(projection.apply(exact.value()),"Projection returned null");
        return new ProjectionResult.Unavailable<>(((ProjectionResult.Unavailable<?>)result).reason());
    }
    public static RegistryAdapter<NamespacedKey> keys(RegistryCatalog catalog){return new RegistryAdapter<>(catalog,entry->new ProjectionResult.Exact<>(new NamespacedKey(entry.key().entryNamespace(),entry.key().entryPath())));}
}
