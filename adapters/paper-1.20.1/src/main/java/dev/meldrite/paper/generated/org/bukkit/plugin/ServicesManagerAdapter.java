package dev.meldrite.paper.generated.org.bukkit.plugin;

import dev.meldrite.bridge.ExecutionPort;
import dev.meldrite.plugin.spi.PluginScope;
import dev.meldrite.service.ServiceDirectory;
import java.util.*;
import java.util.function.*;
import org.bukkit.event.Event;
import org.bukkit.event.server.*;
import org.bukkit.plugin.*;

public final class ServicesManagerAdapter implements ServicesManager,AutoCloseable {
    private final ServiceDirectory<RegisteredServiceProvider<?>> directory;
    private final Function<Plugin,PluginScope> scopes;
    public ServicesManagerAdapter(ExecutionPort execution,Function<Plugin,PluginScope> scopes,Consumer<Event> events){
        this.scopes=Objects.requireNonNull(scopes);Objects.requireNonNull(events);
        directory=new ServiceDirectory<>(execution,change->events.accept(change.registered()?new ServiceRegisterEvent(change.metadata()):new ServiceUnregisterEvent(change.metadata())));
    }
    public <T> void register(Class<T> service,T provider,Plugin plugin,ServicePriority priority){
        Objects.requireNonNull(plugin);Objects.requireNonNull(priority);
        var metadata=new RegisteredServiceProvider<>(service,provider,priority,plugin);
        directory.register(service,provider,priority.ordinal(),scopes.apply(plugin),metadata);
    }
    public void unregisterAll(Plugin plugin){Objects.requireNonNull(plugin);directory.remove(value->value.metadata().getPlugin()==plugin);}
    public void unregister(Class<?> service,Object provider){Objects.requireNonNull(service);directory.remove(value->value.type()==service&&value.provider()==provider);}
    public void unregister(Object provider){directory.remove(value->value.provider()==provider);}
    public <T> T load(Class<T> service){return directory.load(service).orElse(null);}
    public <T> RegisteredServiceProvider<T> getRegistration(Class<T> service){return getRegistrations(service).stream().findFirst().orElse(null);}
    public List<RegisteredServiceProvider<?>> getRegistrations(Plugin plugin){Objects.requireNonNull(plugin);return directory.snapshot().stream().<RegisteredServiceProvider<?>>map(value->value.metadata()).filter(value->value.getPlugin()==plugin).toList();}
    @SuppressWarnings("unchecked") public <T> Collection<RegisteredServiceProvider<T>> getRegistrations(Class<T> service){Objects.requireNonNull(service);return directory.snapshot().stream().filter(value->value.type()==service).map(value->(RegisteredServiceProvider<T>)value.metadata()).toList();}
    public Collection<Class<?>> getKnownServices(){var result=new LinkedHashSet<Class<?>>();for(var value:directory.snapshot())result.add(value.type());return Collections.unmodifiableSet(result);}
    public <T> boolean isProvidedFor(Class<T> service){return directory.load(service).isPresent();}
    @Override public void close(){directory.close();}
}
