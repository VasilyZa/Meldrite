package dev.meldrite.foundation;

import java.util.Objects;
import java.util.regex.Pattern;

public record ResourceKey(String registryNamespace, String registryPath, String entryNamespace, String entryPath) {
    private static final Pattern NAMESPACE = Pattern.compile("[a-z0-9_.-]+");
    private static final Pattern PATH = Pattern.compile("[a-z0-9/._-]+");

    public ResourceKey {
        registryNamespace = component(registryNamespace, NAMESPACE, "registry namespace");
        registryPath = component(registryPath, PATH, "registry path");
        entryNamespace = component(entryNamespace, NAMESPACE, "entry namespace");
        entryPath = component(entryPath, PATH, "entry path");
        if (invalidPath(registryPath) || invalidPath(entryPath)) throw new IllegalArgumentException("Invalid resource path");
    }
    private static String component(String value, Pattern pattern, String label) {
        Objects.requireNonNull(value, label);
        if (!pattern.matcher(value).matches()) throw new IllegalArgumentException("Invalid " + label + ": " + value);
        return value;
    }
    private static boolean invalidPath(String path) { return path.startsWith("/") || path.endsWith("/") || path.contains(".."); }
}
