package dev.meldrite.api;

import dev.meldrite.foundation.ResourceKey;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/** Version-neutral view of content registered by a native or API provider. */
public interface ContentAccess {
    Optional<Content> find(ResourceKey id);

    record Content(ResourceKey id, String kind, Set<String> tags) {
        public Content {
            Objects.requireNonNull(id); Objects.requireNonNull(kind); Objects.requireNonNull(tags);
            if (kind.isBlank()) throw new IllegalArgumentException("kind must not be blank");
            tags = Set.copyOf(tags);
        }
    }
}
