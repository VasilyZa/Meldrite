package dev.meldrite.bridge;
import dev.meldrite.foundation.*;
import java.util.Objects;
import java.util.Optional;
public interface EntityPort {
    record EntityInfo(EntityHandle handle, EntityPosition position, ResourceKey type, boolean player, String name) {
        public EntityInfo { Objects.requireNonNull(handle); Objects.requireNonNull(position); Objects.requireNonNull(type); Objects.requireNonNull(name); }
    }
    boolean isValid(EntityHandle entity);
    default Optional<EntityInfo> describe(EntityHandle entity) { return Optional.empty(); }
    default void remove(EntityHandle entity){throw new UnsupportedOperationException("UNSUPPORTED_CAPABILITY: entity.remove");}
    default EntityPosition position(EntityHandle entity){throw new UnsupportedOperationException("UNSUPPORTED_CAPABILITY: entity.position");}
    default ResourceKey type(EntityHandle entity){throw new UnsupportedOperationException("UNSUPPORTED_CAPABILITY: entity.type");}
    /** Atomic same-world movement. False leaves position and other native effects unchanged. */
    default boolean teleport(EntityHandle entity,EntityPosition position){throw new UnsupportedOperationException("UNSUPPORTED_CAPABILITY: entity.teleport");}
    /** Sends a server-authored chat line through the native player connection. */
    default void sendMessage(EntityHandle entity, String message){throw new UnsupportedOperationException("UNSUPPORTED_CAPABILITY: entity.chat");}
    /** Unknown/unavailable types fail before creating any native entity. */
    default EntitySpawnReceipt spawn(ResourceKey type,EntityPosition position){throw new UnsupportedOperationException("UNSUPPORTED_CAPABILITY: entity.spawn");}
}
