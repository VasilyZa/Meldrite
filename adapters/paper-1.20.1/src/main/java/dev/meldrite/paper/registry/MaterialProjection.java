package dev.meldrite.paper.registry;
import dev.meldrite.foundation.ResourceKey;
import dev.meldrite.registry.*;
import java.util.*;
import org.bukkit.Material;
/** Exact bindings derived from the authoritative enum after native registry publication. */
public final class MaterialProjection {
    private final FixedProjection<Material> projection;
    @SuppressWarnings("deprecation") public MaterialProjection(){
        Map<ResourceKey,Material> map=new LinkedHashMap<>();
        for(Material material:Material.values())if(!material.isLegacy()){
            var key=material.getKey();
            if(material.isItem())map.put(new ResourceKey("minecraft","item",key.getNamespace(),key.getKey()),material);
            if(material.isBlock())map.put(new ResourceKey("minecraft","block",key.getNamespace(),key.getKey()),material);
        }
        projection=new FixedProjection<>(map);
    }
    public ProjectionResult<Material> block(ResourceKey key){
        if(!key.registryNamespace().equals("minecraft")||!key.registryPath().equals("block"))return new ProjectionResult.Unsupported<>("WRONG_BLOCK_REGISTRY: "+key);
        return projection.project(key);
    }
    public ProjectionResult<Material> project(ResourceKey key){return projection.project(key);}
    public ProjectionResult<Material> project(RegistrySnapshot.Entry entry){return projection.project(entry);}
}
