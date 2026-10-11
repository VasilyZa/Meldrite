package org.bukkit.craftbukkit.v1_20_R1.scheduler;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.PluginBase;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginLogger;

/** Internal plugin identity used for vanilla scheduler tasks. */
public final class MinecraftInternalPlugin extends PluginBase {
    private final PluginDescriptionFile description = new PluginDescriptionFile("Minecraft", "1.0", "nms");
    private volatile boolean enabled = true;
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    @Override public File getDataFolder() { throw new UnsupportedOperationException("Minecraft has no plugin data folder"); }
    @Override public PluginDescriptionFile getDescription() { return description; }
    @Override public io.papermc.paper.plugin.configuration.PluginMeta getPluginMeta() { return description; }
    @Override public FileConfiguration getConfig() { throw new UnsupportedOperationException("Minecraft has no plugin config"); }
    @Override public InputStream getResource(String filename) { return null; }
    @Override public void saveConfig() { throw new UnsupportedOperationException("Minecraft has no plugin config"); }
    @Override public void saveDefaultConfig() { throw new UnsupportedOperationException("Minecraft has no plugin config"); }
    @Override public void saveResource(String path, boolean replace) { throw new UnsupportedOperationException("Minecraft has no plugin resources"); }
    @Override public void reloadConfig() { throw new UnsupportedOperationException("Minecraft has no plugin config"); }
    @Override public PluginLogger getLogger() { return new PluginLogger(this); }
    @Override public PluginLoader getPluginLoader() { return null; }
    @Override public Server getServer() { return org.bukkit.Bukkit.getServer(); }
    @Override public boolean isEnabled() { return enabled; }
    @Override public void onDisable() { enabled = false; }
    @Override public void onLoad() { }
    @Override public void onEnable() { enabled = true; }
    @Override public boolean isNaggable() { return false; }
    @Override public void setNaggable(boolean canNag) { }
    @Override public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) { return null; }
    @Override public BiomeProvider getDefaultBiomeProvider(String worldName, String id) { return null; }
    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) { return false; }
    @Override public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) { return List.of(); }
}
