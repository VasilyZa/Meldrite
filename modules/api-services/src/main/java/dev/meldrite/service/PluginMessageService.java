package dev.meldrite.service;

import dev.meldrite.bridge.*;
import dev.meldrite.foundation.EntityHandle;
import dev.meldrite.plugin.spi.PluginScope;
import java.util.*;
import java.util.function.*;

/** One scope-owned binary channel registry and dispatch algorithm. */
public final class PluginMessageService<P,R> implements AutoCloseable {
    public record Incoming(EntityHandle player,String channel,byte[] payload){
        public Incoming{Objects.requireNonNull(player);Objects.requireNonNull(channel);payload=Objects.requireNonNull(payload).clone();}
        @Override public byte[] payload(){return payload.clone();}
    }
    public final class Registration implements AutoCloseable {
        private final P owner;private final PluginScope scope;private final String channel;
        private final Optional<R> metadata;private final Optional<Consumer<Incoming>> listener;
        private boolean removed;
        private Registration(P owner,PluginScope scope,String channel,Optional<R> metadata,Optional<Consumer<Incoming>> listener){this.owner=owner;this.scope=scope;this.channel=channel;this.metadata=metadata;this.listener=listener;}
        public P owner(){return owner;}public String channel(){return channel;}public Optional<R> metadata(){return metadata;}
        private boolean live(){return !removed&&!scope.isClosed();}
        public boolean isClosed(){synchronized(PluginMessageService.this){return removed;}}
        @Override public void close(){removeOwned(value->value==this);}
    }
    private final ExecutionPort execution;private final PluginMessagePort transport;private final int maximumBytes;
    private final BiConsumer<R,Throwable> failures;private final List<Registration> registrations=new ArrayList<>();private boolean closed;
    public PluginMessageService(ExecutionPort execution,PluginMessagePort transport,int maximumBytes,BiConsumer<R,Throwable> failures){this.execution=Objects.requireNonNull(execution);this.transport=Objects.requireNonNull(transport);if(maximumBytes<0)throw new IllegalArgumentException("Negative payload bound");this.maximumBytes=maximumBytes;this.failures=Objects.requireNonNull(failures);}
    private void primary(){if(!execution.isPrimaryThread())throw new IllegalStateException("ASYNC_PLUGIN_MESSAGE_MUTATION");}
    private void open(){if(closed)throw new IllegalStateException("PLUGIN_MESSAGE_SERVICE_CLOSED");}
    private static String channel(String channel){Objects.requireNonNull(channel);if(channel.isEmpty())throw new IllegalArgumentException("Empty plugin channel");return channel;}
    private void payload(byte[] payload){Objects.requireNonNull(payload);if(payload.length>maximumBytes)throw new IllegalArgumentException("PLUGIN_PAYLOAD_TOO_LARGE");}
    public Registration outgoing(P owner,PluginScope scope,String channel){return register(owner,scope,channel,Optional.empty(),Optional.empty());}
    public Registration incoming(P owner,PluginScope scope,String channel,R metadata,Consumer<Incoming> listener){return register(owner,scope,channel,Optional.of(metadata),Optional.of(listener));}
    private Registration register(P owner,PluginScope scope,String channel,Optional<R> metadata,Optional<Consumer<Incoming>> listener){
        Registration registration;
        synchronized(this){primary();open();Objects.requireNonNull(owner);Objects.requireNonNull(scope);channel(channel);if(scope.isClosed())throw new IllegalStateException("OWNER_DISABLED");
            for(var value:registrations)if(value.live()&&value.owner==owner&&value.channel.equals(channel)&&value.metadata.equals(metadata)){
                if(metadata.isPresent())throw new IllegalArgumentException("DUPLICATE_PLUGIN_LISTENER");return value;
            }
            registration=new Registration(owner,scope,channel,metadata,listener);registrations.add(registration);
        }
        return scope.register(registration);
    }
    public synchronized List<Registration> snapshot(){open();return registrations.stream().filter(Registration::live).toList();}
    public synchronized void remove(Predicate<Registration> selection){primary();open();removeOwned(selection);}
    private synchronized void removeOwned(Predicate<Registration> selection){primary();Objects.requireNonNull(selection);for(var value:registrations)if(selection.test(value))value.removed=true;registrations.removeIf(value->value.removed);}
    public void dispatch(EntityHandle player,String channel,byte[] bytes){
        primary();Objects.requireNonNull(player);channel(channel);payload(bytes);connected(player);
        var input=new Incoming(player,channel,bytes);
        for(var value:snapshot())if(value.channel.equals(channel)&&value.listener.isPresent()){
            synchronized(this){if(!value.live())continue;}
            try{value.listener.orElseThrow().accept(input);}catch(Throwable failure){if(failure instanceof InterruptedException)Thread.currentThread().interrupt();try{failures.accept(value.metadata.orElseThrow(),failure);}catch(Throwable reporting){if(reporting instanceof InterruptedException)Thread.currentThread().interrupt();if(reporting!=failure)failure.addSuppressed(reporting);if(failure instanceof Error error)throw error;throw failure instanceof RuntimeException runtime?runtime:new IllegalStateException("PLUGIN_MESSAGE_LISTENER_FAILED",failure);}}
        }
    }
    private void connected(EntityHandle player){if(!transport.isConnected(Objects.requireNonNull(player)))throw new IllegalStateException("STALE_PLUGIN_MESSAGE_RECIPIENT");}
    public void send(P owner,PluginScope scope,EntityHandle recipient,String channel,byte[] bytes){
        primary();Objects.requireNonNull(scope);channel(channel);payload(bytes);
        if(scope.isClosed())throw new IllegalStateException("OWNER_DISABLED");
        if(snapshot().stream().noneMatch(value->value.owner==owner&&value.channel.equals(channel)&&value.metadata.isEmpty()))throw new IllegalStateException("PLUGIN_CHANNEL_NOT_REGISTERED");
        connected(recipient);transport.send(recipient,channel,bytes.clone());
    }
    public Set<String> listeningChannels(EntityHandle recipient){primary();synchronized(this){open();}connected(recipient);return Set.copyOf(transport.listeningChannels(recipient));}
    @Override public synchronized void close(){primary();if(closed)return;closed=true;for(var value:registrations)value.removed=true;registrations.clear();}
}
