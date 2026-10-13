package dev.meldrite.paper;
import dev.meldrite.plugin.spi.VisibilityPolicy;
import io.papermc.paper.plugin.provider.classloader.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
/** Typed protocol adapter; search permissions and algorithms are shared neutral policies. */
final class ConfiguredClassLoaderGroupAdapter implements PluginClassLoaderGroup {
    private final MeldriteConfiguredClassLoader owner;
    private final Map<String,MeldriteConfiguredClassLoader> registered;
    private final VisibilityPolicy policy;
    private final Set<String> excluded=ConcurrentHashMap.newKeySet();
    ConfiguredClassLoaderGroupAdapter(MeldriteConfiguredClassLoader owner,Map<String,MeldriteConfiguredClassLoader> registered,VisibilityPolicy policy){this.owner=owner;this.registered=registered;this.policy=policy;}
    public ClassLoaderAccess getAccess(){return loader->{var available=owner.groupProviders();String id=loader.getConfiguration().getName();return available.get(id)==loader&&!excluded.contains(id)&&(loader==owner||policy.routes(owner.pluginId(),available.keySet()).contains(id));};}
    public Class<?> getClassByName(String name,boolean resolve,ConfiguredPluginClassLoader requester){
        if(requester!=owner)throw new IllegalArgumentException("FOREIGN_GROUP_REQUESTER");
        var available=owner.groupProviders();
        for(String id:policy.routes(owner.pluginId(),available.keySet())){var candidate=available.get(id);if(candidate==null||excluded.contains(id))continue;
            try{return candidate.loadClass(name,resolve,false,policy.exportLibraries(owner.pluginId(),id));}
            catch(ClassNotFoundException absent){if(absent.getCause()!=null)throw new IllegalStateException("Group class definition failed: "+name,absent);}
        }
        return null; // The pinned group ABI explicitly uses null to report no class match.
    }
    public void add(ConfiguredPluginClassLoader loader){requireOwned(loader);excluded.remove(loader.getConfiguration().getName());((PhaseVisibilityPolicy)policy).include(loader.getConfiguration().getName());}
    public void remove(ConfiguredPluginClassLoader loader){requireOwned(loader);excluded.add(loader.getConfiguration().getName());((PhaseVisibilityPolicy)policy).exclude(loader.getConfiguration().getName());}
    private void requireOwned(ConfiguredPluginClassLoader loader){if(owner.groupProviders().get(loader.getConfiguration().getName())!=loader)throw new IllegalArgumentException("FOREIGN_PLUGIN_REALM");}
}
