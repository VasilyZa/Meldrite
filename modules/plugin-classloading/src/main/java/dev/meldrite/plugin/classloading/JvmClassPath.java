package dev.meldrite.plugin.classloading;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.*;
import java.nio.file.Path;
import java.util.*;
import sun.misc.Unsafe;

/** Read-only snapshots of the URL source; locating it never exports existing parent classes. */
public final class JvmClassPath {
    private JvmClassPath() {}
    public static List<Path> paths(ClassLoader loader) throws IOException {
        Objects.requireNonNull(loader);
        for (ClassLoader current = loader; current != null; current = current.getParent())
            if (current instanceof URLClassLoader urls) return paths(urls.getURLs());
        try {
            Object ucp = read(loader, "ucp");
            if (ucp == null) throw new IOException("HOST_CLASSPATH_UNAVAILABLE: " + loader.getClass().getName());
            Object value = read(ucp, "path");
            if (!(value instanceof List<?> values)) throw new IOException("HOST_CLASSPATH_LAYOUT_UNSUPPORTED: " + ucp.getClass().getName());
            Object unopened = read(ucp, "unopenedUrls");
            if (unopened == null) throw new IOException("HOST_CLASSPATH_LAYOUT_UNSUPPORTED: " + ucp.getClass().getName());
            Object[] snapshot;
            synchronized (ucp) { synchronized (unopened) { snapshot = values.toArray(); } }
            URL[] urls = new URL[snapshot.length];
            for (int i = 0; i < snapshot.length; i++) {
                if (!(snapshot[i] instanceof URL url)) throw new IOException("HOST_CLASSPATH_ENTRY_UNSUPPORTED: " + snapshot[i]);
                urls[i] = url;
            }
            return paths(urls);
        } catch (ReflectiveOperationException | RuntimeException failure) {
            throw new IOException("HOST_CLASSPATH_LAYOUT_UNSUPPORTED: " + loader.getClass().getName(), failure);
        }
    }
    private static List<Path> paths(URL[] urls) throws IOException {
        var result = new ArrayList<Path>();
        for (URL url : urls) {
            if (!url.getProtocol().equals("file")) throw new IOException("UNSUPPORTED_HOST_LIBRARY_URL: " + url);
            try { result.add(Path.of(url.toURI()).toAbsolutePath().normalize()); }
            catch (URISyntaxException | IllegalArgumentException failure) { throw new IOException("INVALID_HOST_LIBRARY_URL: " + url, failure); }
        }
        return List.copyOf(result);
    }
    private static Object read(Object owner, String name) throws ReflectiveOperationException {
        Class<?> type = owner.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField(name);
                if (field.getType().isPrimitive() || java.lang.reflect.Modifier.isStatic(field.getModifiers()))
                    throw new IllegalArgumentException("Expected an instance reference field: " + field);
                Field access = Unsafe.class.getDeclaredField("theUnsafe"); access.setAccessible(true);
                Unsafe unsafe = (Unsafe) access.get(null);
                return unsafe.getObject(owner, unsafe.objectFieldOffset(field));
            } catch (NoSuchFieldException absent) { type = type.getSuperclass(); }
        }
        throw new NoSuchFieldException(owner.getClass().getName() + "." + name);
    }
}
