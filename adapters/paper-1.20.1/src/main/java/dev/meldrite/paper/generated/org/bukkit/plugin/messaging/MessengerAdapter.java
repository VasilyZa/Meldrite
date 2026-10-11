package dev.meldrite.paper.generated.org.bukkit.plugin.messaging;

import dev.meldrite.foundation.EntityHandle;
import dev.meldrite.plugin.classloading.*;
import dev.meldrite.plugin.spi.PluginScope;
import dev.meldrite.service.PluginMessageService;
import java.util.*;
import java.util.function.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.*;

/** Locked API validation and type conversion; common code owns registrations/dispatch. */
public final class MessengerAdapter implements Messenger {
    private final PluginMessageService<Plugin,PluginMessageListenerRegistration> messages;
    private final Function<Plugin,PluginScope> scopes;private final PluginLoaderEngine engine;
    private final Function<Player,EntityHandle> handles;private final Function<EntityHandle,Player> players;
    public MessengerAdapter(PluginMessageService<Plugin,PluginMessageListenerRegistration> messages,Function<Plugin,PluginScope> scopes,PluginLoaderEngine engine,Function<Player,EntityHandle> handles,Function<EntityHandle,Player> players){this.messages=Objects.requireNonNull(messages);this.scopes=Objects.requireNonNull(scopes);this.engine=Objects.requireNonNull(engine);this.handles=Objects.requireNonNull(handles);this.players=Objects.requireNonNull(players);}
    private static Plugin plugin(Plugin plugin){if(plugin==null)throw new IllegalArgumentException("Plugin cannot be null");return plugin;}
    static String canonical(String channel){String validated=StandardMessenger.validateAndCorrectChannel(channel);return validated.equals("BungeeCord")?"bungeecord:main":validated;}
    private static void listener(PluginMessageListener listener){if(listener==null)throw new IllegalArgumentException("Listener cannot be null");}
    public boolean isReservedChannel(String channel){String key=canonical(channel);return key.equals("minecraft:register")||key.equals("minecraft:unregister");}
    private String registerChannel(String channel){String key=canonical(channel);if(isReservedChannel(key))throw new ReservedChannelException(channel);return key;}
    public void registerOutgoingPluginChannel(Plugin plugin,String channel){plugin(plugin);messages.outgoing(plugin,scopes.apply(plugin),registerChannel(channel));}
    public void unregisterOutgoingPluginChannel(Plugin plugin,String channel){plugin(plugin);String key=canonical(channel);messages.remove(value->value.owner()==plugin&&value.metadata().isEmpty()&&value.channel().equals(key));}
    public void unregisterOutgoingPluginChannel(Plugin plugin){plugin(plugin);messages.remove(value->value.owner()==plugin&&value.metadata().isEmpty());}
    public PluginMessageListenerRegistration registerIncomingPluginChannel(Plugin plugin,String channel,PluginMessageListener listener){
        plugin(plugin);String key=registerChannel(channel);listener(listener);ClassLoader loader=plugin.getClass().getClassLoader();
        if(!(loader instanceof PluginRealm realm))throw new UnsupportedOperationException("UNSUPPORTED_PLUGIN_REALM: "+loader);
        var registration=new PluginMessageListenerRegistration(this,plugin,key,listener);
        var callback=new PluginMessageListenerAdapter(engine,realm,listener);
        messages.incoming(plugin,scopes.apply(plugin),key,registration,input->callback.onPluginMessageReceived(input.channel(),players.apply(input.player()),input.payload()));
        return registration;
    }
    public void unregisterIncomingPluginChannel(Plugin plugin,String channel,PluginMessageListener listener){plugin(plugin);listener(listener);String key=canonical(channel);messages.remove(value->value.owner()==plugin&&value.channel().equals(key)&&value.metadata().map(registration->registration.getListener().equals(listener)).orElse(false));}
    public void unregisterIncomingPluginChannel(Plugin plugin,String channel){plugin(plugin);String key=canonical(channel);messages.remove(value->value.owner()==plugin&&value.channel().equals(key)&&value.metadata().isPresent());}
    public void unregisterIncomingPluginChannel(Plugin plugin){plugin(plugin);messages.remove(value->value.owner()==plugin&&value.metadata().isPresent());}
    private Set<String> channels(Predicate<PluginMessageService<Plugin,PluginMessageListenerRegistration>.Registration> selection){var result=new LinkedHashSet<String>();for(var value:messages.snapshot())if(selection.test(value))result.add(value.channel());return Collections.unmodifiableSet(result);}
    public Set<String> getOutgoingChannels(){return channels(value->value.metadata().isEmpty());}
    public Set<String> getOutgoingChannels(Plugin plugin){plugin(plugin);return channels(value->value.owner()==plugin&&value.metadata().isEmpty());}
    public Set<String> getIncomingChannels(){return channels(value->value.metadata().isPresent());}
    public Set<String> getIncomingChannels(Plugin plugin){plugin(plugin);return channels(value->value.owner()==plugin&&value.metadata().isPresent());}
    private Set<PluginMessageListenerRegistration> registrations(Predicate<PluginMessageService<Plugin,PluginMessageListenerRegistration>.Registration> selection){var result=new LinkedHashSet<PluginMessageListenerRegistration>();for(var value:messages.snapshot())if(selection.test(value))value.metadata().ifPresent(result::add);return Collections.unmodifiableSet(result);}
    public Set<PluginMessageListenerRegistration> getIncomingChannelRegistrations(Plugin plugin){plugin(plugin);return registrations(value->value.owner()==plugin);}
    public Set<PluginMessageListenerRegistration> getIncomingChannelRegistrations(String channel){String key=canonical(channel);return registrations(value->value.channel().equals(key));}
    public Set<PluginMessageListenerRegistration> getIncomingChannelRegistrations(Plugin plugin,String channel){plugin(plugin);String key=canonical(channel);return registrations(value->value.owner()==plugin&&value.channel().equals(key));}
    public boolean isRegistrationValid(PluginMessageListenerRegistration registration){if(registration==null)throw new IllegalArgumentException("Registration cannot be null");return getIncomingChannelRegistrations(registration.getPlugin(),registration.getChannel()).contains(registration);}
    public boolean isIncomingChannelRegistered(Plugin plugin,String channel){plugin(plugin);return getIncomingChannels(plugin).contains(canonical(channel));}
    public boolean isOutgoingChannelRegistered(Plugin plugin,String channel){plugin(plugin);return getOutgoingChannels(plugin).contains(canonical(channel));}
    public void dispatchIncomingMessage(Player source,String channel,byte[] message){if(source==null)throw new IllegalArgumentException("Player cannot be null");if(message==null)throw new IllegalArgumentException("Message cannot be null");if(message.length>MAX_MESSAGE_SIZE)throw new MessageTooLargeException(message);messages.dispatch(handles.apply(source),canonical(channel),message);}
}
