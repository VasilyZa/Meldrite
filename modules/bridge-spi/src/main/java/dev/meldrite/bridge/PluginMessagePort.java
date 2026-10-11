package dev.meldrite.bridge;

import dev.meldrite.foundation.EntityHandle;
import java.util.Set;

/** Binary plugin channels are separate from Adventure text transport. */
public interface PluginMessagePort {
    boolean isConnected(EntityHandle recipient);
    void send(EntityHandle recipient, String channel, byte[] payload);
    Set<String> listeningChannels(EntityHandle recipient);
}
