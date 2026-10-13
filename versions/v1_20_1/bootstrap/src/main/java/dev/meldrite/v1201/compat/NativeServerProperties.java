package dev.meldrite.v1201.compat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Properties;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.dedicated.ServerPropertiesHandler;

/** Re-read the native settings without writing the previous generation back to disk. */
public final class NativeServerProperties {
    private NativeServerProperties() { }

    public static void reload(MinecraftDedicatedServer server) {
        var loader = server.propertiesLoader;
        var source = new Properties();
        try {
            try (var reader = Files.newBufferedReader(loader.path, StandardCharsets.UTF_8)) {
                source.load(reader);
            } catch (CharacterCodingException legacyEncoding) {
                source.clear();
                try (var reader = Files.newBufferedReader(loader.path, StandardCharsets.ISO_8859_1)) {
                    source.load(reader);
                }
            }
        } catch (IOException failure) {
            throw new UncheckedIOException("Failed to reload server properties: " + loader.path, failure);
        }
        // The native handler parses defaults and validates the property types.
        var properties = new ServerPropertiesHandler(source);
        loader.propertiesHandler = properties;
        server.setMotd(properties.motd);
        server.setPvpEnabled(properties.pvp);
        server.setFlightEnabled(properties.allowFlight);
        server.setEnforceWhitelist(properties.enforceWhitelist);
        var players = server.getPlayerManager();
        players.maxPlayers = properties.maxPlayers;
        // The dedicated setter persists the file; reload already read that file.
        players.whitelistEnabled = properties.whiteList.get();
        players.setViewDistance(properties.viewDistance);
        players.setSimulationDistance(properties.simulationDistance);
    }
}
