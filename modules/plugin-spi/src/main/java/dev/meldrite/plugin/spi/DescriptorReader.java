package dev.meldrite.plugin.spi;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
/** Profile-owned syntax and precedence. Discovery never loads a plugin class. */
public interface DescriptorReader {
    List<String> descriptorNames();
    PluginDescriptor read(Path source, Map<String,String> documents, int discoveryIndex);
}
