package dev.meldrite.v1201.compat;

import java.time.Instant;
import java.util.Objects;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;

/** Converts the real signed-message metadata and edits only its unsigned presentation. */
public final class NativeChatMessage {
    private static final GsonComponentSerializer GSON = GsonComponentSerializer.gson();
    private NativeChatMessage() { }

    public static Component component(Text text) { return GSON.deserialize(Text.Serializer.toJson(Objects.requireNonNull(text))); }
    public static Text text(Component component) { return Objects.requireNonNull(Text.Serializer.fromJson(GSON.serialize(component))); }

    public static net.kyori.adventure.chat.SignedMessage adventure(SignedMessage message) {
        return new Projection(message.getTimestamp(), message.getSalt(), message.signature() == null ? null
                : net.kyori.adventure.chat.SignedMessage.signature(message.signature().data().clone()),
                message.unsignedContent() == null ? null : component(message.unsignedContent()),
                message.getSignedContent(), Identity.identity(message.getSender()));
    }

    /** SentMessage.send remains responsible for mask enabling and fully-filtered suppression per recipient. */
    public static SignedMessage forViewer(SignedMessage original, Component rendered, boolean shouldFilter) {
        if (shouldFilter && !original.filterMask().isPassThrough()) return original;
        return original.withUnsignedContent(text(rendered));
    }

    private record Projection(Instant timestamp, long salt, net.kyori.adventure.chat.SignedMessage.Signature signature,
            Component unsignedContent, String message, Identity identity) implements net.kyori.adventure.chat.SignedMessage { }
}
