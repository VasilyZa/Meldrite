package dev.meldrite.compat;

import java.util.Objects;

public interface CompatibilityPack {
    String id();
    String minecraftVersion();
    String profile();
    String targetJarHash();
    default boolean enabled() { return true; }
    void apply(CompatibilityContext context) throws Exception;
    default void audit(CompatibilityContext context) throws Exception { }
    default void validate() { if (id() == null || id().isBlank()) throw new IllegalArgumentException("pack id must not be blank"); Objects.requireNonNull(minecraftVersion()); Objects.requireNonNull(profile()); }
}
