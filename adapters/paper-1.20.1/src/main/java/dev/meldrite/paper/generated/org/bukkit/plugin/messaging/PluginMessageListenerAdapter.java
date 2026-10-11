package dev.meldrite.paper.generated.org.bukkit.plugin.messaging;

import dev.meldrite.plugin.classloading.*;
import java.util.Objects;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

public final class PluginMessageListenerAdapter implements PluginMessageListener {
    private final PluginLoaderEngine engine;private final PluginRealm realm;private final PluginMessageListener listener;
    public PluginMessageListenerAdapter(PluginLoaderEngine engine,PluginRealm realm,PluginMessageListener listener){this.engine=Objects.requireNonNull(engine);this.realm=Objects.requireNonNull(realm);this.listener=Objects.requireNonNull(listener);}
    public void onPluginMessageReceived(String channel,Player player,byte[] message){
        Objects.requireNonNull(channel);Objects.requireNonNull(player);Objects.requireNonNull(message);
        try{engine.withContext(realm,()->{listener.onPluginMessageReceived(channel,player,message);return player;});}
        catch(RuntimeException|Error failure){throw failure;}catch(Exception failure){throw new IllegalStateException("PLUGIN_MESSAGE_CALLBACK_FAILED",failure);}
    }
}
