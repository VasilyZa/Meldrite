package dev.meldrite.paper.bootstrap;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/** The selected providers share Knot's definition authority; no unrestricted parent route is exposed. */
public final class ApiProviderLocator {
    private ApiProviderLocator() {}
    private static Properties providers() throws IOException {
        var properties = new Properties();
        try (var input = ApiProviderLocator.class.getClassLoader().getResourceAsStream("meldrite/api-providers.properties")) {
            if (input == null) throw new IOException("API_PROVIDER_MANIFEST_MISSING");
            properties.load(input);
        }
        if (properties.isEmpty()) throw new IOException("API_PROVIDER_MANIFEST_EMPTY");
        return properties;
    }
    public static Set<String> duplicates() throws IOException {
        Set<String> duplicates = new TreeSet<>();
        ClassLoader loader = ApiProviderLocator.class.getClassLoader();
        Properties providers = providers();
        for (String provider : providers.stringPropertyNames()) {
            String marker = providers.getProperty(provider);
            Set<String> locations = new HashSet<>();
            var resources = loader.getResources(marker.replace('.', '/') + ".class");
            while (resources.hasMoreElements()) locations.add(resources.nextElement().toExternalForm());
            if (locations.isEmpty()) throw new IOException("API_PROVIDER_MISSING: " + provider);
            if (locations.size() > 1) duplicates.add(provider);
        }
        return Set.copyOf(duplicates);
    }
    public static List<Path> paths() throws Exception {
        Set<Path> paths = new LinkedHashSet<>();
        Properties providers = providers();
        for (String provider : new TreeSet<>(providers.stringPropertyNames())) {
            Class<?> marker = Class.forName(providers.getProperty(provider), false, ApiProviderLocator.class.getClassLoader());
            var source = marker.getProtectionDomain().getCodeSource();
            if (source == null) throw new IOException("API_PROVIDER_SOURCE_MISSING: " + provider);
            paths.add(Path.of(source.getLocation().toURI()));
        }
        return List.copyOf(paths);
    }
}
