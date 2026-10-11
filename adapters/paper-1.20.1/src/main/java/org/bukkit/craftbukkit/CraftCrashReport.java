package org.bukkit.craftbukkit;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.function.Supplier;
import org.bukkit.Bukkit;

/** Crash report supplement without depending on a versioned server class. */
public final class CraftCrashReport implements Supplier<String> {
    @Override public String get() {
        StringWriter output = new StringWriter();
        try {
            output.append("\n   Running: ").append(Bukkit.getName()).append(" version ").append(Bukkit.getVersion())
                    .append(" (Implementing API version ").append(Bukkit.getBukkitVersion()).append(")");
            output.append("\n   Plugins: {");
            for (var plugin : Bukkit.getPluginManager().getPlugins()) {
                output.append(' ').append(plugin.getDescription().getFullName()).append(' ')
                        .append(plugin.getDescription().getMain()).append(' ')
                        .append(Arrays.toString(plugin.getDescription().getAuthors().toArray())).append(',');
            }
            output.append("}\n   Warnings: ").append(Bukkit.getWarningState().name());
            output.append("\n   Threads: {");
            for (var entry : Thread.getAllStackTraces().entrySet()) {
                output.append(' ').append(entry.getKey().getState().name()).append(' ')
                        .append(entry.getKey().getName()).append(": ").append(Arrays.toString(entry.getValue())).append(',');
            }
            output.append("}");
        } catch (Throwable failure) {
            output.append("\n   Failed to handle CraftCrashReport:\n");
            failure.printStackTrace(new PrintWriter(output));
        }
        return output.toString();
    }
}
