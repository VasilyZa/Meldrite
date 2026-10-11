package dev.meldrite.registry;
import dev.meldrite.foundation.ResourceKey;
import java.util.*;
/** Closed enum projection has explicit exact bindings and never chooses a replacement value. */
public final class FixedProjection<T> {
    private final Map<ResourceKey,T> bindings;
    public FixedProjection(Map<ResourceKey,T> bindings){this.bindings=Map.copyOf(bindings);}
    public ProjectionResult<T> project(RegistrySnapshot.Entry entry){
        return project(entry.key());
    }
    public ProjectionResult<T> project(ResourceKey key){
        T value=bindings.get(key);
        return value==null?new ProjectionResult.Unsupported<>("FIXED_ENUM_UNREPRESENTABLE: "+key):new ProjectionResult.Exact<>(value);
    }
}
