package dev.meldrite.plugin.spi;
@FunctionalInterface
public interface ClassTransform {
    byte[] apply(byte[] source, TransformContext context) throws Exception;
    default byte[] prepare(byte[] source, TransformContext context) throws Exception { return source; }
    /** Optional declaration-index pass before a standalone library is prepared. */
    default void prepareLibrary(java.nio.file.Path source, TransformContext context) throws Exception {}
}
