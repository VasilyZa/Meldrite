package org.bukkit.craftbukkit.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/** Reads and persists the stable UUID used by a world directory. */
public final class WorldUUID {
    private WorldUUID() {
    }

    public static UUID getUUID(java.io.File baseDir) {
        return getUUID(baseDir.toPath());
    }

    public static UUID getUUID(Path baseDir) {
        Path file = baseDir.resolve("uid.dat");
        try (DataInputStream input = new DataInputStream(Files.newInputStream(file))) {
            return new UUID(input.readLong(), input.readLong());
        } catch (IOException ignored) {
            // Generate and persist a UUID when this is a new or damaged world.
        }
        UUID uuid = UUID.randomUUID();
        try {
            Files.createDirectories(baseDir);
            try (DataOutputStream output = new DataOutputStream(Files.newOutputStream(file))) {
                output.writeLong(uuid.getMostSignificantBits());
                output.writeLong(uuid.getLeastSignificantBits());
            }
        } catch (IOException ignored) {
            // The generated value remains valid for this process.
        }
        return uuid;
    }
}
