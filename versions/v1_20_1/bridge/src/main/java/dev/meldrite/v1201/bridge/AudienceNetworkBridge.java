package dev.meldrite.v1201.bridge;

import dev.meldrite.bridge.AudienceMessage;
import dev.meldrite.bridge.NetworkAudiencePort;

import java.util.Objects;
import java.util.function.Consumer;

/** Single serialized transport boundary for Adventure/native audience messages. */
public final class AudienceNetworkBridge implements NetworkAudiencePort {
    private final Consumer<AudienceMessage> transport;

    public AudienceNetworkBridge(Consumer<AudienceMessage> transport) { this.transport = Objects.requireNonNull(transport, "transport"); }

    @Override public void send(String audienceId, byte[] serializedComponent) {
        Objects.requireNonNull(audienceId, "audienceId");
        Objects.requireNonNull(serializedComponent, "serializedComponent");
        send(new AudienceMessage(new AudienceMessage.Target("player", audienceId, 0),
                AudienceMessage.Channel.SYSTEM_MESSAGE, java.util.List.of(serializedComponent),
                java.util.Optional.empty(), java.util.Optional.empty()));
    }

    @Override public void send(AudienceMessage message) { transport.accept(Objects.requireNonNull(message, "message")); }
}
