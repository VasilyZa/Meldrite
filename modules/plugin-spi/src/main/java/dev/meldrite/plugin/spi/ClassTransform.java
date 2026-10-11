package dev.meldrite.plugin.spi;
@FunctionalInterface
public interface ClassTransform {
    byte[] apply(byte[] source, TransformContext context) throws Exception;
    default byte[] prepare(byte[] source, TransformContext context) throws Exception { return source; }
}
