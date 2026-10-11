package dev.meldrite.plugin.spi;
import java.util.Objects;
public record TransformContext(String pluginId, Access access) {
    public enum Access { API_ONLY, INTERNAL }
    public TransformContext { if(Objects.requireNonNull(pluginId).isBlank())throw new IllegalArgumentException("Blank plugin id");Objects.requireNonNull(access); }
}
