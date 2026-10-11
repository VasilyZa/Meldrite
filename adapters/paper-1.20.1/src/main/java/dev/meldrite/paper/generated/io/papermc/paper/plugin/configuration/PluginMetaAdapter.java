package dev.meldrite.paper.generated.io.papermc.paper.plugin.configuration;
public final class PluginMetaAdapter implements io.papermc.paper.plugin.configuration.PluginMeta {
private final io.papermc.paper.plugin.configuration.PluginMeta metadata;
public PluginMetaAdapter(io.papermc.paper.plugin.configuration.PluginMeta metadata){this.metadata=java.util.Objects.requireNonNull(metadata);}
  @Override public java.lang.String getAPIVersion() { return metadata.getAPIVersion(); }
  @Override public java.util.List<java.lang.String> getAuthors() { return metadata.getAuthors(); }
  @Override public java.util.List<java.lang.String> getContributors() { return metadata.getContributors(); }
  @Override public java.lang.String getDescription() { return metadata.getDescription(); }
  @Override public java.util.List<java.lang.String> getLoadBeforePlugins() { return metadata.getLoadBeforePlugins(); }
  @Override public org.bukkit.plugin.PluginLoadOrder getLoadOrder() { return metadata.getLoadOrder(); }
  @Override public java.lang.String getLoggerPrefix() { return metadata.getLoggerPrefix(); }
  @Override public java.lang.String getMainClass() { return metadata.getMainClass(); }
  @Override public java.lang.String getName() { return metadata.getName(); }
  @Override public org.bukkit.permissions.PermissionDefault getPermissionDefault() { return metadata.getPermissionDefault(); }
  @Override public java.util.List<org.bukkit.permissions.Permission> getPermissions() { return metadata.getPermissions(); }
  @Override public java.util.List<java.lang.String> getPluginDependencies() { return metadata.getPluginDependencies(); }
  @Override public java.util.List<java.lang.String> getPluginSoftDependencies() { return metadata.getPluginSoftDependencies(); }
  @Override public java.util.List<java.lang.String> getProvidedPlugins() { return metadata.getProvidedPlugins(); }
  @Override public java.lang.String getVersion() { return metadata.getVersion(); }
  @Override public java.lang.String getWebsite() { return metadata.getWebsite(); }
}
