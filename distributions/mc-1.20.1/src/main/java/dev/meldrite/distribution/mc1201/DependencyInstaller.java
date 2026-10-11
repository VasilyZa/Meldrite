package dev.meldrite.distribution.mc1201;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static dev.meldrite.distribution.mc1201.ServerLauncher.required;

/** Keeps downloaded and embedded artifacts in the same content-addressed cache. */
final class DependencyInstaller {
    private static final boolean CHINESE = Locale.getDefault(Locale.Category.DISPLAY).getLanguage().equals("zh");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

    private DependencyInstaller() { }

    static List<Path> install(Properties bundle, Path directory) throws Exception {
        int count = Integer.parseInt(required(bundle, "artifactCount"));
        if (count < 1) throw new IOException("Empty dependency manifest");
        Path cache = directory.resolve("libraries/meldrite").normalize();
        List<Path> artifacts = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            Path target = directory.resolve(required(bundle, index + ".path")).normalize();
            if (!target.startsWith(cache) || target.equals(cache)) {
                throw new IOException("Invalid dependency path: " + target);
            }
            artifacts.add(target);
        }
        var pool = Executors.newFixedThreadPool(Math.min(8, count), runnable -> {
            Thread thread = new Thread(runnable, "meldrite-dependency-download");
            thread.setDaemon(true);
            return thread;
        });
        try {
            List<Future<?>> pending = new ArrayList<>();
            for (int index = 0; index < count; index++) {
                int artifact = index;
                pending.add(pool.submit(() -> {
                    installArtifact(bundle, artifact, artifacts.get(artifact));
                    return null;
                }));
            }
            for (Future<?> future : pending) {
                try {
                    future.get();
                } catch (ExecutionException failed) {
                    if (failed.getCause() instanceof Exception cause) throw cause;
                    throw new IOException("Dependency installation failed", failed.getCause());
                }
            }
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw interrupted;
        } finally {
            pool.shutdownNow();
        }
        return artifacts;
    }

    private static void installArtifact(Properties bundle, int index, Path target) throws Exception {
        String prefix = index + ".";
        String sha256 = required(bundle, prefix + "sha256");
        if (!sha256.matches("[0-9a-f]{64}")) throw new IOException("Invalid dependency checksum: " + target);
        long size = Long.parseLong(bundle.getProperty(prefix + "size", "-1"));
        if (valid(target, sha256, size)) return;
        Files.createDirectories(target.getParent());
        Path temporary = Files.createTempFile(target.getParent(), "install-", ".tmp");
        try {
            String resource = bundle.getProperty(prefix + "resource");
            if (resource != null && !resource.isBlank()) {
                try (InputStream input = DependencyInstaller.class.getClassLoader().getResourceAsStream(resource)) {
                    if (input == null) throw new IOException("Missing bundled resource: " + resource);
                    Files.copy(input, temporary, StandardCopyOption.REPLACE_EXISTING);
                }
                if (!valid(temporary, sha256, size)) throw new IOException("Bundled checksum mismatch: " + resource);
            } else {
                int sources = Integer.parseInt(required(bundle, prefix + "urlCount"));
                IOException failed = new IOException("Cannot download dependency: " + target.getFileName());
                boolean downloaded = false;
                for (int source = 0; source < sources; source++) {
                    if (Thread.currentThread().isInterrupted()) throw new InterruptedException();
                    String url = required(bundle, prefix + "url." + source);
                    try {
                        log((CHINESE ? "下载依赖 " : "Downloading ") + target.getFileName());
                        download(URI.create(url), temporary, target.getFileName().toString(), size);
                        if (!valid(temporary, sha256, size)) throw new IOException("SHA-256/size mismatch: " + url);
                        downloaded = true;
                        break;
                    } catch (IOException problem) {
                        failed.addSuppressed(problem);
                        log((CHINESE ? "下载失败，尝试下一来源: " : "Download failed, trying next source: ") + problem.getMessage());
                    }
                }
                if (!downloaded) throw failed;
                log((CHINESE ? "已下载并校验 " : "Downloaded and verified ") + target.getFileName());
            }
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException unsupported) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static void download(URI initial, Path target, String name, long expectedSize) throws IOException {
        var visited = new HashSet<URI>();
        URI uri = initial;
        while (visited.size() < 8 && visited.add(uri)) {
            if (!"https".equalsIgnoreCase(uri.getScheme()) && !"http".equalsIgnoreCase(uri.getScheme())) {
                throw new IOException("Unsupported download URL: " + uri);
            }
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setConnectTimeout(15_000);
            connection.setReadTimeout(30_000);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("User-Agent", "Meldrite-Launcher");
            try {
                int status = connection.getResponseCode();
                if (status == 301 || status == 302 || status == 303 || status == 307 || status == 308) {
                    String location = connection.getHeaderField("Location");
                    if (location == null) throw new IOException("Redirect without Location: " + uri);
                    uri = uri.resolve(location);
                    continue;
                }
                if (status != 200) throw new IOException("HTTP " + status + ": " + uri);
                try (InputStream input = connection.getInputStream(); OutputStream output = Files.newOutputStream(target)) {
                    byte[] buffer = new byte[8192];
                    long received = 0;
                    long nextReport = System.nanoTime() + 5_000_000_000L;
                    int length;
                    while ((length = input.read(buffer)) != -1) {
                        if (Thread.currentThread().isInterrupted()) throw new InterruptedIOException("Download interrupted: " + uri);
                        received += length;
                        if (expectedSize >= 0 && received > expectedSize) throw new IOException("Download exceeds pinned size: " + uri);
                        output.write(buffer, 0, length);
                        if (expectedSize > 1_048_576 && System.nanoTime() >= nextReport) {
                            log((CHINESE ? "下载进度 " : "Download progress ") + name + ": "
                                    + (received * 100 / expectedSize) + "%");
                            nextReport = System.nanoTime() + 5_000_000_000L;
                        }
                    }
                }
                return;
            } finally {
                connection.disconnect();
            }
        }
        throw new IOException("Too many redirects or redirect loop: " + uri);
    }

    private static boolean valid(Path path, String sha256, long size) throws Exception {
        if (!Files.isRegularFile(path) || (size >= 0 && Files.size(path) != size)) return false;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream input = new DigestInputStream(Files.newInputStream(path), digest)) {
            input.transferTo(OutputStream.nullOutputStream());
        }
        return HexFormat.of().formatHex(digest.digest()).equals(sha256);
    }

    private static void log(String message) {
        System.out.println("[" + LocalTime.now().format(TIME) + "] [Meldrite Launcher/INFO]: " + message);
    }
}
