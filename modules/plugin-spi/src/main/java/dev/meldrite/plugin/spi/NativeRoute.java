package dev.meldrite.plugin.spi;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
/** Explicit host export allowlist; INTERNAL access is also required by the loader engine. */
public record NativeRoute(ClassLoader loader,Set<String> classes,Set<String> resources,Function<String,String> classResolver) {
    private static final Function<String,String> IDENTITY=name -> null;
    public NativeRoute(ClassLoader loader,Set<String> classes,Set<String> resources){this(loader,classes,resources,IDENTITY);}
    public NativeRoute {
        Objects.requireNonNull(loader);
        classes=Set.copyOf(classes);
        resources=Set.copyOf(resources);
        classResolver=Objects.requireNonNull(classResolver);
    }
    /** Returns the runtime binary name for a requested plugin-side name, or null when unsupported. */
    public String target(String name) {
        if (classes.contains(name)) return name;
        String resolved=classResolver.apply(name);
        return resolved==null||resolved.isBlank()?null:resolved;
    }
}
