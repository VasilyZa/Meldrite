package dev.meldrite.paper;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.configuration.PluginMeta;
import java.nio.file.Path;
import java.util.Objects;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
/** This pinned BootstrapContext has metadata/files/logger only, and exposes no world port. */
public record BootstrapContextAdapter(PluginMeta getConfiguration,Path getDataDirectory,ComponentLogger getLogger,Path getPluginSource) implements BootstrapContext {
    public BootstrapContextAdapter {Objects.requireNonNull(getConfiguration);Objects.requireNonNull(getDataDirectory);Objects.requireNonNull(getLogger);Objects.requireNonNull(getPluginSource);}
}
