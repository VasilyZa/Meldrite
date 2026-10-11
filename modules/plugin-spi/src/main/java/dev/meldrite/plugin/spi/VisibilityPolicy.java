package dev.meldrite.plugin.spi;
import java.util.Collection;
import java.util.List;
public interface VisibilityPolicy {
    List<String> routes(String consumer, Collection<String> available);
    boolean exportLibraries(String consumer, String provider);
    void accessed(String consumer, String provider, String symbol);
}
