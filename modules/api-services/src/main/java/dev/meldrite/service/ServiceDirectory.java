package dev.meldrite.service;

import dev.meldrite.bridge.ExecutionPort;
import dev.meldrite.plugin.spi.PluginScope;
import java.util.*;
import java.util.function.*;

/** Class identity, not class name, identifies a service across isolated plugin realms. */
public final class ServiceDirectory<M> implements AutoCloseable {
    public record Change<M>(boolean registered, M metadata) {}
    public final class Registration<T> implements AutoCloseable {
        private final Class<T> type;
        private final T provider;
        private final int priority;
        private final PluginScope scope;
        private final M metadata;
        private boolean removed;
        private Registration(Class<T> type,T provider,int priority,PluginScope scope,M metadata) {
            this.type=type;this.provider=provider;this.priority=priority;this.scope=scope;this.metadata=metadata;
        }
        public Class<T> type(){return type;}
        public T provider(){return provider;}
        public M metadata(){return metadata;}
        private boolean live(){return !removed&&!scope.isClosed();}
        @Override public void close(){removeOwned(registration->registration==this);}
    }
    private final ExecutionPort execution;
    private final Consumer<Change<M>> notifications;
    private final Map<Class<?>,List<Registration<?>>> registrations=new LinkedHashMap<>();
    private boolean closed;
    public ServiceDirectory(ExecutionPort execution,Consumer<Change<M>> notifications){this.execution=Objects.requireNonNull(execution);this.notifications=Objects.requireNonNull(notifications);}
    private void primary(){if(!execution.isPrimaryThread())throw new IllegalStateException("ASYNC_SERVICE_MUTATION");}
    private void open(){if(closed)throw new IllegalStateException("SERVICE_DIRECTORY_CLOSED");}
    public <T> Registration<T> register(Class<T> type,T provider,int priority,PluginScope scope,M metadata){
        Registration<T> registration;
        synchronized(this){primary();open();Objects.requireNonNull(type);type.cast(Objects.requireNonNull(provider));Objects.requireNonNull(scope);Objects.requireNonNull(metadata);if(scope.isClosed())throw new IllegalStateException("OWNER_DISABLED");
            registration=new Registration<>(type,provider,priority,scope,metadata);
            // Same priority insertion follows the pinned API's binary-search rule.
            var providers=registrations.computeIfAbsent(type,key->new ArrayList<>());
            int index=Collections.binarySearch(providers,registration,Comparator.comparingInt((Registration<?> value)->value.priority).reversed());
            providers.add(index<0?-index-1:index,registration);
        }
        scope.register(registration);
        notifications.accept(new Change<>(true,metadata));
        return registration;
    }
    public synchronized List<Registration<?>> snapshot(){open();return registrations.values().stream().flatMap(Collection::stream).filter(Registration::live).toList();}
    public <T> Optional<T> load(Class<T> type){Objects.requireNonNull(type);return snapshot().stream().filter(value->value.type==type).findFirst().map(value->type.cast(value.provider));}
    public void remove(Predicate<Registration<?>> selection){
        synchronized(this){primary();open();}removeOwned(selection);
    }
    private void removeOwned(Predicate<Registration<?>> selection){
        List<Registration<?>> removed;
        synchronized(this){primary();Objects.requireNonNull(selection);removed=registrations.values().stream().flatMap(Collection::stream).filter(selection).toList();for(var value:removed)value.removed=true;for(var values:registrations.values())values.removeAll(removed);registrations.values().removeIf(List::isEmpty);}
        Throwable first=null;
        for(var value:removed)try{notifications.accept(new Change<>(false,value.metadata));}catch(Throwable failure){if(first==null)first=failure;else if(first!=failure)first.addSuppressed(failure);}
        if(first!=null)throw propagate(first);
    }
    private static RuntimeException propagate(Throwable failure){if(failure instanceof InterruptedException)Thread.currentThread().interrupt();if(failure instanceof Error error)throw error;return failure instanceof RuntimeException runtime?runtime:new IllegalStateException("SERVICE_NOTIFICATION_FAILED",failure);}
    @Override public void close(){synchronized(this){primary();if(closed)return;closed=true;}removeOwned(value->true);}
}
