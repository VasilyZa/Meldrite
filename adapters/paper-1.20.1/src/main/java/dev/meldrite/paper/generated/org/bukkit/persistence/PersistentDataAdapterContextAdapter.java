package dev.meldrite.paper.generated.org.bukkit.persistence;
import dev.meldrite.service.PersistentDataService;
import dev.meldrite.plugin.classloading.PluginLoaderEngine;
import java.util.Objects;
import org.bukkit.persistence.*;
public final class PersistentDataAdapterContextAdapter implements PersistentDataAdapterContext {
    private final PersistentDataService service;private final PluginLoaderEngine engine;
    public PersistentDataAdapterContextAdapter(PersistentDataService service,PluginLoaderEngine engine){this.service=Objects.requireNonNull(service);this.engine=Objects.requireNonNull(engine);}
    public PersistentDataContainer newPersistentDataContainer(){return new PersistentDataContainerAdapter(service.container(),engine);}
}
