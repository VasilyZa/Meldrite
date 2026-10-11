package dev.meldrite.paper.generated.org.bukkit.plugin.messaging;

import dev.meldrite.foundation.EntityHandle;
import dev.meldrite.plugin.spi.PluginScope;
import dev.meldrite.service.PluginMessageService;
import java.util.*;
import java.util.function.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.*;

public final class PluginMessageRecipientAdapter implements PluginMessageRecipient {
    private final Messenger messenger;private final PluginMessageService<Plugin,PluginMessageListenerRegistration> messages;
    private final Supplier<EntityHandle> recipient;private final Function<Plugin,PluginScope> scopes;
    public PluginMessageRecipientAdapter(Messenger messenger,PluginMessageService<Plugin,PluginMessageListenerRegistration> messages,Supplier<EntityHandle> recipient,Function<Plugin,PluginScope> scopes){this.messenger=Objects.requireNonNull(messenger);this.messages=Objects.requireNonNull(messages);this.recipient=Objects.requireNonNull(recipient);this.scopes=Objects.requireNonNull(scopes);}
    public void sendPluginMessage(Plugin source,String channel,byte[] message){StandardMessenger.validatePluginMessage(messenger,source,channel,message);messages.send(source,scopes.apply(source),recipient.get(),MessengerAdapter.canonical(channel),message);}
    public Set<String> getListeningPluginChannels(){return messages.listeningChannels(recipient.get());}
}
