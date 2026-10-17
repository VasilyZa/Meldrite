package dev.meldrite.plugin.spi;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/** Explicit boundary between libraries appended to an API host and the server definition realm. */
public interface GlobalLibraryAccess extends AutoCloseable {
    ClassLoader sourceLoader();
    List<Path> paths() throws IOException;
    ClassLoader runtimeLoader();
    void append(Path sourceJar, Path preparedJar, ClassTransform transform, TransformContext context) throws IOException;
    /** The runtime loader itself remains owned by the server. */
    @Override default void close() throws IOException {}
}
