package dev.meldrite.v1201.bootstrap;

import dev.meldrite.paper.compat.LegacyMappings;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;

/** Publishes Paper's standard reflection mapping resource for the active Fabric namespace. */
final class NativeReflectionMappings {
    private NativeReflectionMappings() {}

    static void install(Path cache) throws IOException {
        var mappings = new ByteArrayOutputStream();
        LegacyMappings.writePaperReflectionMappings(mappings);
        var archive = new ByteArrayOutputStream();
        try (var jar = new JarOutputStream(archive)) {
            var entry = new JarEntry("META-INF/mappings/reobf.tiny");
            entry.setTime(0);
            jar.putNextEntry(entry);
            mappings.writeTo(jar);
            jar.closeEntry();
        }
        byte[] bytes = archive.toByteArray();
        String hash;
        try { hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)); }
        catch (NoSuchAlgorithmException failure) { throw new IllegalStateException("SHA-256 unavailable", failure); }
        Files.createDirectories(cache);
        Path destination = cache.resolve(hash + ".jar");
        if (Files.exists(destination)) {
            if (!Arrays.equals(bytes, Files.readAllBytes(destination)))
                throw new IOException("REFLECTION_MAPPINGS_CACHE_CORRUPT: " + destination);
        } else {
            Path temporary = Files.createTempFile(cache, "mappings-", ".jar");
            try {
                Files.write(temporary, bytes);
                Files.move(temporary, destination, StandardCopyOption.ATOMIC_MOVE);
            } finally { Files.deleteIfExists(temporary); }
        }
        FabricLauncherBase.getLauncher().addToClassPath(destination.toAbsolutePath());
    }
}
