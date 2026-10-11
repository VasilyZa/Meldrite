package dev.meldrite.paper;
import dev.meldrite.plugin.classloading.PluginLoaderEngine;
import dev.meldrite.plugin.spi.*;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import java.util.*;
import org.bukkit.plugin.java.JavaPlugin;
public final class PluginHostAdapter implements PluginHost {
    private final PluginDescriptor descriptor;
    private final PluginScope scope;
    private final PluginLoaderEngine engine;
    private final MeldriteConfiguredClassLoader loader;
    private final BootstrapContextAdapter context;
    private Optional<PluginBootstrap> bootstrap=Optional.empty();
    private Optional<JavaPlugin> plugin=Optional.empty();
    private Optional<Throwable> bootstrapFailure=Optional.empty();
    public PluginHostAdapter(PluginDescriptor descriptor,PluginScope scope,PluginLoaderEngine engine,MeldriteConfiguredClassLoader loader,BootstrapContextAdapter context){this.descriptor=descriptor;this.scope=scope;this.engine=engine;this.loader=loader;this.context=context;scope.register(loader);}
    public PluginIdentity identity(){return descriptor.identity();}public PluginScope scope(){return scope;}
    public MeldriteConfiguredClassLoader loader(){return loader;}
    public void bootstrap()throws Exception {if(descriptor.bootstrapper().isPresent())engine.withContext(loader,()->{PluginBootstrap boot=loader.loadClass(descriptor.bootstrapper().orElseThrow()).asSubclass(PluginBootstrap.class).getConstructor().newInstance();boot.bootstrap(context);bootstrap=Optional.of(boot);return boot;});}
    public PluginDescriptor descriptor(){return descriptor;}
    public Optional<Throwable> bootstrapFailure(){return bootstrapFailure;}
    @Override public Optional<Throwable> preparationFailure(){return bootstrapFailure;}
    public void rejectBootstrap(Throwable failure){bootstrapFailure=Optional.of(failure);try{scope.close();}catch(Throwable cleanup){if(cleanup!=failure)failure.addSuppressed(cleanup);}}
    public void construct()throws Exception {if(bootstrapFailure.isPresent())throw new IllegalStateException("PLUGIN_BOOTSTRAP_FAILED: "+identity().id(),bootstrapFailure.get());if(plugin.isPresent())throw new IllegalStateException("Plugin already constructed");engine.withContext(loader,()->{JavaPlugin created=bootstrap.isPresent()?bootstrap.get().createPlugin(context):loader.loadClass(descriptor.mainClass()).asSubclass(JavaPlugin.class).getConstructor().newInstance();if(created==null||created.getClass().getClassLoader()!=loader||created!=loader.getPlugin())throw new IllegalStateException("INVALID_BOOTSTRAP_PLUGIN_INSTANCE: "+identity().id());plugin=Optional.of(created);return created;});}
    public JavaPlugin plugin(){return plugin.orElseThrow(()->new IllegalStateException("PLUGIN_NOT_CONSTRUCTED: "+identity().id()));}
    public void load()throws Exception {engine.withContext(loader,()->{plugin().onLoad();return plugin();});}
    public void enable()throws Exception {engine.withContext(loader,()->{plugin().setEnabled(true);return plugin();});}
    public void disable()throws Exception {engine.withContext(loader,()->{plugin().setEnabled(false);return plugin();});}
}
