package dev.meldrite.bridge;
public interface NetworkAudiencePort {
    void send(String audienceId, byte[] serializedComponent);
    default void send(AudienceMessage message) {
        throw new UnsupportedOperationException("UNSUPPORTED_CAPABILITY: audience.channel-transport");
    }
}
