package dev.meldrite.paper.configuration;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * The three server-level YAML documents exposed by the Spigot/Paper API.
 * They are owned by the profile directory and are deliberately shared by
 * Server.Spigot and the historical SpigotConfig entry point.
 */
public final class ServerConfiguration {
    private static volatile ServerConfiguration current;

    private final Path directory;
    private final File bukkitFile;
    private final File spigotFile;
    private final File paperFile;
    private final YamlConfiguration bukkit;
    private final YamlConfiguration spigot;
    private final YamlConfiguration paper;

    private ServerConfiguration(Path directory) throws IOException {
        this.directory = directory.toAbsolutePath().normalize();
        Files.createDirectories(this.directory);
        this.bukkitFile = this.directory.resolve("bukkit.yml").toFile();
        this.spigotFile = this.directory.resolve("spigot.yml").toFile();
        this.paperFile = this.directory.resolve("paper.yml").toFile();
        this.bukkit = YamlConfiguration.loadConfiguration(bukkitFile);
        this.spigot = YamlConfiguration.loadConfiguration(spigotFile);
        this.paper = YamlConfiguration.loadConfiguration(paperFile);
    }

    public static synchronized ServerConfiguration initialize(Path directory) throws IOException {
        Objects.requireNonNull(directory, "directory");
        ServerConfiguration existing = current;
        Path normalized = directory.toAbsolutePath().normalize();
        if (existing != null && existing.directory.equals(normalized)) return existing;
        ServerConfiguration created = new ServerConfiguration(normalized);
        current = created;
        return created;
    }

    public static ServerConfiguration current() {
        ServerConfiguration value = current;
        if (value == null) throw new IllegalStateException("SERVER_CONFIGURATION_NOT_INITIALIZED");
        return value;
    }

    public static synchronized void clear() {
        current = null;
    }

    public Path directory() { return directory; }
    public YamlConfiguration bukkit() { return bukkit; }
    public YamlConfiguration spigot() { return spigot; }
    public YamlConfiguration paper() { return paper; }

    public synchronized void save() throws IOException {
        bukkit.save(bukkitFile);
        spigot.save(spigotFile);
        paper.save(paperFile);
    }
}
