package org.spigotmc;

import dev.meldrite.paper.configuration.ServerConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * Spigot's configuration surface backed by the server configuration.
 * Values come from the profile's persisted spigot.yml; no marker-only class
 * or hard-coded server detection is used.
 */
public final class SpigotConfig {
    private SpigotConfig() {}

    public static volatile YamlConfiguration config;
    public static volatile int version;
    public static volatile boolean logCommands;
    public static volatile int tabComplete;
    public static volatile boolean sendNamespaced;
    public static volatile String whitelistMessage;
    public static volatile String unknownCommandMessage;
    public static volatile String serverFullMessage;
    public static volatile String outdatedClientMessage = "Outdated client! Please use {0}";
    public static volatile String outdatedServerMessage = "Outdated server! I\\'m still on {0}";
    public static volatile int timeoutTime = 60;
    public static volatile boolean restartOnCrash = true;
    public static volatile String restartScript = "./start.sh";
    public static volatile String restartMessage;
    public static volatile boolean bungee;
    public static volatile int playerSample;
    public static volatile int playerShuffle;
    public static volatile List<String> spamExclusions = List.of("/skill");
    public static volatile boolean silentCommandBlocks;
    public static volatile Set<String> replaceCommands = Set.of("setblock", "summon", "testforblock", "tellraw");
    public static volatile int userCacheCap = 1000;
    public static volatile boolean saveUserCacheOnStopOnly;
    public static volatile double movedWronglyThreshold = 0.0625D;
    public static volatile double movedTooQuicklyMultiplier = 10.0D;
    public static volatile boolean debug;
    public static volatile boolean disableAdvancementSaving;
    public static volatile List<String> disabledAdvancements = List.of("minecraft:story/disabled");
    public static volatile boolean logVillagerDeaths = true;
    public static volatile boolean logNamedDeaths = true;
    public static volatile boolean disablePlayerDataSaving;
    public static volatile boolean belowZeroGenerationInExistingChunks = true;

    public static synchronized void init(File configFile) {
        if (configFile == null) throw new IllegalArgumentException("configFile");
        try {
            ServerConfiguration store = ServerConfiguration.initialize(configFile.toPath().toAbsolutePath().normalize().getParent());
            config = store.spigot();
            version = getInt("config-version", 12);
            config.set("config-version", version);
            loadValues();
            store.save();
        } catch (IOException | RuntimeException failure) {
            logger().log(Level.SEVERE, "Could not load spigot.yml", failure);
            throw new IllegalStateException("SPIGOT_CONFIG_INIT_FAILED", failure);
        }
    }

    public static synchronized void readConfig(Class<?> ignored, Object instance) {
        ensureInitialized();
        loadValues();
        save();
    }

    public static synchronized void save() {
        ensureInitialized();
        try {
            ServerConfiguration.current().save();
        } catch (IOException failure) {
            logger().log(Level.SEVERE, "Could not save spigot.yml", failure);
            throw new IllegalStateException("SPIGOT_CONFIG_SAVE_FAILED", failure);
        }
    }

    public static boolean getBoolean(String path, boolean def) { ensureInitialized(); config.addDefault(path, def); return config.getBoolean(path, def); }
    public static int getInt(String path, int def) { ensureInitialized(); config.addDefault(path, def); return config.getInt(path, def); }
    public static double getDouble(String path, double def) { ensureInitialized(); config.addDefault(path, def); return config.getDouble(path, def); }
    public static String getString(String path, String def) { ensureInitialized(); config.addDefault(path, def); return config.getString(path, def); }
    public static List<String> getList(String path, List<String> def) { ensureInitialized(); config.addDefault(path, def); return config.getStringList(path); }

    private static void loadValues() {
        logCommands = getBoolean("commands.log", true);
        tabComplete = getInt("commands.tab-complete", 0);
        sendNamespaced = getBoolean("commands.send-namespaced", true);
        whitelistMessage = transform(getString("messages.whitelist", "You are not whitelisted on this server!"));
        unknownCommandMessage = transform(getString("messages.unknown-command", "Unknown command. Type \\\"/help\\\" for help."));
        serverFullMessage = transform(getString("messages.server-full", "The server is full!"));
        outdatedClientMessage = transform(getString("messages.outdated-client", outdatedClientMessage));
        outdatedServerMessage = transform(getString("messages.outdated-server", outdatedServerMessage));
        timeoutTime = getInt("settings.timeout-time", timeoutTime);
        restartOnCrash = getBoolean("settings.restart-on-crash", restartOnCrash);
        restartScript = getString("settings.restart-script", restartScript);
        restartMessage = transform(getString("messages.restart", "Server is restarting"));
        bungee = getBoolean("settings.bungeecord", false);
        playerSample = Math.max(getInt("settings.sample-count", 12), 0);
        playerShuffle = getInt("settings.player-shuffle", 0);
        spamExclusions = getList("commands.spam-exclusions", List.of("/skill"));
        silentCommandBlocks = getBoolean("commands.silent-commandblock-console", false);
        replaceCommands = new HashSet<>(getList("commands.replace-commands", Arrays.asList("setblock", "summon", "testforblock", "tellraw")));
        userCacheCap = getInt("settings.user-cache-size", 1000);
        saveUserCacheOnStopOnly = getBoolean("settings.save-user-cache-on-stop-only", false);
        movedWronglyThreshold = getDouble("settings.moved-wrongly-threshold", movedWronglyThreshold);
        movedTooQuicklyMultiplier = getDouble("settings.moved-too-quickly-multiplier", movedTooQuicklyMultiplier);
        debug = getBoolean("settings.debug", false);
        disableAdvancementSaving = getBoolean("advancements.disable-saving", false);
        disabledAdvancements = getList("advancements.disabled", List.of("minecraft:story/disabled"));
        logVillagerDeaths = getBoolean("settings.log-villager-deaths", true);
        logNamedDeaths = getBoolean("settings.log-named-deaths", true);
        disablePlayerDataSaving = getBoolean("players.disable-saving", false);
        belowZeroGenerationInExistingChunks = getBoolean("world-settings.default.below-zero-generation-in-existing-chunks", true);
    }

    private static String transform(String value) { return ChatColor.translateAlternateColorCodes('&', value).replace("\\\\n", "\n"); }
    private static void ensureInitialized() { if (config == null) throw new IllegalStateException("SPIGOT_CONFIG_NOT_INITIALIZED"); }
    private static java.util.logging.Logger logger() { return Bukkit.getLogger(); }
}
