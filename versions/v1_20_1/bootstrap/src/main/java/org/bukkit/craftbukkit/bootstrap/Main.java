package org.bukkit.craftbukkit.bootstrap;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/** Bundler entry point compatible with the vanilla server bootstrap contract. */
public final class Main {
    public static void main(String[] args) { new Main().run(args); }
    private void run(String[] args) {
        try {
            String main = read("main-class");
            if (main == null || main.isBlank()) throw new IllegalStateException("Missing META-INF/main-class");
            Class<?> type = Class.forName(System.getProperty("bundlerMainClass", main));
            type.getMethod("main", String[].class).invoke(null, (Object) args);
        } catch (Exception failure) {
            throw new IllegalStateException("Unable to launch bundled server", failure);
        }
    }
    private String read(String name) throws Exception {
        try (var stream = Main.class.getResourceAsStream("/META-INF/" + name)) {
            if (stream == null) return null;
            try (var reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) { return reader.readLine(); }
        }
    }
    public record FileEntry(String hash, String id, String path) { public static FileEntry parseLine(String line) { String[] fields = line.split(" "); if (fields.length != 2) throw new IllegalArgumentException("Malformed library entry"); return new FileEntry(fields[0], fields[1], fields[1]); } }
    @FunctionalInterface public interface ResourceParser<T> { T parse(BufferedReader reader) throws Exception; }
    public static final class Thrower {
        private Thrower() { }
        public static void sneakyThrow(Throwable failure) { Thrower.<RuntimeException>sneaky(failure); }
        private static <T extends Throwable> void sneaky(Throwable failure) throws T { throw (T) failure; }
    }
}
