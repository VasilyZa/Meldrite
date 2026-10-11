package org.bukkit.craftbukkit.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.bukkit.Bukkit;

/** Resolves the API version from the same metadata location used by CraftBukkit. */
public final class Versioning {
    private Versioning() {
    }

    public static String getBukkitVersion() {
        String[] resources = {
            "META-INF/maven/io.papermc.paper/paper-api/pom.properties",
            "META-INF/maven/cn.dreeam.leaf/leaf-api/pom.properties",
            "META-INF/maven/org.spigotmc/spigot-api/pom.properties"
        };
        ClassLoader loader = Bukkit.class.getClassLoader();
        for (String resource : resources) {
            try (InputStream stream = loader.getResourceAsStream(resource)) {
                if (stream == null) continue;
                Properties properties = new Properties();
                properties.load(stream);
                String version = properties.getProperty("version");
                if (version != null && !version.isBlank()) return version;
            } catch (IOException ignored) {
                // Try the next known packaging location.
            }
        }
        return "Unknown-Version";
    }
}
