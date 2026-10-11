package dev.meldrite.plugin.spi;

import java.util.Objects;
import java.util.regex.Pattern;

public record PluginIdentity(String id, String version) {
    private static final Pattern ID = Pattern.compile("[A-Za-z0-9][A-Za-z0-9_.-]*");
    public PluginIdentity {
        Objects.requireNonNull(id, "id"); Objects.requireNonNull(version, "version");
        if (!ID.matcher(id).matches() || version.isBlank()) throw new IllegalArgumentException("Invalid plugin identity");
    }
}
