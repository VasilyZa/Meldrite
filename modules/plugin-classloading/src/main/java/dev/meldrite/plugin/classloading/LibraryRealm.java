package dev.meldrite.plugin.classloading;
import dev.meldrite.plugin.spi.*;
import java.io.*;
import java.nio.file.Path;
import java.util.*;
public final class LibraryRealm extends OwnedRealm {
    static { registerAsParallelCapable(); }
    private final java.util.List<NativeRoute> nativeRoutes;
    LibraryRealm(List<Path> jars,ApiAuthority authority,TransformContext context,ClassTransform transform,
                 java.util.List<NativeRoute> nativeRoutes)throws IOException {
        super(jars,authority,context,transform);
        this.nativeRoutes = java.util.List.copyOf(nativeRoutes);
    }
    LibraryRealm(List<Path> jars,ApiAuthority authority,TransformContext context,ClassTransform transform)throws IOException {
        this(jars, authority, context, transform, java.util.List.of());
    }
    @Override protected Class<?> loadClass(String name,boolean resolve)throws ClassNotFoundException {
        String normalized = name.replace('/', '.');
        if (normalized.equals(LegacyClassLookup.class.getName())) return LegacyClassLookup.class;
        synchronized(getClassLoadingLock(normalized)) {
            Class<?> loaded=findLoadedClass(normalized);
            if (loaded == null) {
                if (closed) throw new ClassNotFoundException("REALM_CLOSED: "+context.pluginId());
                for (NativeRoute route : nativeRoutes) {
                    String target = route.target(normalized);
                    if (target != null) {
                        try {
                            loaded = route.loader().loadClass(target);
                            break;
                        } catch (ClassNotFoundException ignored) {
                        }
                    }
                }
                if (loaded == null) loaded=sharedIfPresent(normalized).orElse(null);
                if (loaded == null) loaded=own(normalized);
            }
            if(resolve && loaded.getClassLoader()==this)resolveClass(loaded);
            return loaded;
        }
    }
}
