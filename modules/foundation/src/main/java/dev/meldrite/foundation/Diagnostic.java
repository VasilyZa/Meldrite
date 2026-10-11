package dev.meldrite.foundation;
import java.util.Objects;
import java.util.Optional;
public record Diagnostic(String code, Optional<ResourceKey> resource, String stage, String message, Throwable cause) {
    public Diagnostic {
        Objects.requireNonNull(code, "code"); Objects.requireNonNull(resource, "resource"); Objects.requireNonNull(stage, "stage"); Objects.requireNonNull(message, "message");
        if (code.isBlank() || stage.isBlank() || message.isBlank()) throw new IllegalArgumentException("diagnostic fields must not be blank");
    }
}
