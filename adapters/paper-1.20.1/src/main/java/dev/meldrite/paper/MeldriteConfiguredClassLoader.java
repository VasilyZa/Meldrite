package dev.meldrite.paper;
import dev.meldrite.plugin.classloading.*;
import dev.meldrite.plugin.spi.*;
import io.papermc.paper.plugin.configuration.PluginMeta;
import io.papermc.paper.plugin.provider.classloader.*;
import java.io.*;
import java.nio.file.Path;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;
public final class MeldriteConfiguredClassLoader extends PluginRealm implements ConfiguredPluginClassLoader {
    static {registerAsParallelCapable();}
    private final Server server;
    private final PluginMetaAdapter.Metadata metadata;
    private final Path source,dataDirectory;
    private final Logger logger;
    private JavaPlugin plugin;
    private int initCalls;
    private final PluginClassLoaderGroup group;
    private final Map<String,MeldriteConfiguredClassLoader> registered;
    // Paper consumers discover this field by its exact URLClassLoader type.
    // It must be the managed private library realm used by class/resource routing.
    private final URLClassLoader libraryLoader;
    public MeldriteConfiguredClassLoader(PluginLoaderEngine engine,Path source,LibraryRealm libraries,ApiAuthority authority,TransformContext context,ClassTransform transform,VisibilityPolicy policy,Server server,PluginMetaAdapter.Metadata metadata,Path dataDirectory,Logger logger,Map<String,MeldriteConfiguredClassLoader> registered)throws IOException {
        super(engine,source,libraries,authority,context,transform,policy);this.server=Objects.requireNonNull(server);this.metadata=Objects.requireNonNull(metadata);this.source=source;this.dataDirectory=dataDirectory;this.logger=logger;
        this.registered=registered;this.libraryLoader=libraries;group=new ConfiguredClassLoaderGroupAdapter(this,registered,policy);
    }
    @Override public PluginMeta getConfiguration(){return metadata.meta();}
    @Override public Class<?> loadClass(String name,boolean resolve)throws ClassNotFoundException {return loadRoutedClass(name,resolve,true,true);}
    @Override public Class<?> loadClass(String name,boolean resolve,boolean checkGroup,boolean checkLibraries)throws ClassNotFoundException {return loadRoutedClass(name,resolve,checkGroup,checkLibraries);}
    @Override public URL getResource(String name){
        try{var resources=localResources(name);return resources.isEmpty()?null:resources.get(0);}
        catch(IOException failure){throw new UncheckedIOException(failure);}
    }
    @Override public Enumeration<URL> getResources(String name)throws IOException {return Collections.enumeration(localResources(name));}
    @Override public synchronized void init(JavaPlugin plugin){if(this.plugin!=null)throw new IllegalStateException("DUPLICATE_JAVA_PLUGIN_INIT: "+getConfiguration().getName());if(plugin.getClass().getClassLoader()!=this)throw new IllegalArgumentException("FOREIGN_PLUGIN_INSTANCE");plugin.init(server,metadata.description(),dataDirectory.toFile(),source.toFile(),this,metadata.meta(),logger);this.plugin=plugin;initCalls++;}
    @Override public synchronized JavaPlugin getPlugin(){return plugin;}
    @Override public PluginClassLoaderGroup getGroup(){return group;}
    Map<String, MeldriteConfiguredClassLoader> groupProviders() {
        var providers = new LinkedHashMap<String, MeldriteConfiguredClassLoader>();
        routingRealms().forEach((id, realm) -> {
            if (realm instanceof MeldriteConfiguredClassLoader configured) providers.put(id, configured);
        });
        return providers;
    }
    public synchronized int initializationCount(){return initCalls;}
    @Override public void close() throws IOException {
        registered.remove(getConfiguration().getName(), this);
        super.close();
    }
    @Override protected void closeResources() throws IOException {
        try { super.closeResources(); }
        finally { synchronized (this) { plugin = null; } }
    }

}
