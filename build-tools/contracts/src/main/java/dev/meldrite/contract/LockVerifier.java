package dev.meldrite.contract;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** Verifies only bytes present in the lock's local cache. */
public final class LockVerifier {
    private LockVerifier() {}

    public static String verify(ArtifactLock lock, Path projectRoot) throws IOException {
        verifyTree(lock, projectRoot.toAbsolutePath().normalize());
        return "PASS";
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0 || !args[0].equals("verify")) {
            throw new IllegalArgumentException("Usage: LockVerifier verify <lock-file>... [--project-root <path>]");
        }
        Path projectRoot = Path.of(".").toAbsolutePath().normalize();
        java.util.List<Path> locks = new java.util.ArrayList<>();
        for (int i = 1; i < args.length; i++) {
            if ((args[i].equals("--project-root") || args[i].equals("--cache")) && i + 1 < args.length) projectRoot = Path.of(args[++i]).toAbsolutePath().normalize();
            else if (args[i].equals("--central-repository") && i + 1 < args.length) i++;
            else locks.add(Path.of(args[i]));
        }
        if (locks.isEmpty()) throw new IllegalArgumentException("At least one lock file is required");
        for (Path lockPath : locks) {
            ArtifactLockFile lockFile = ArtifactLockFile.read(lockPath);
            if (lockFile.roots().isEmpty()) throw new IOException("Lock file has no roots: " + lockPath);
            for (ArtifactLock lock : lockFile.roots()) verifyTree(lock, projectRoot);
            System.out.println("Verified " + lockFile.roots().size() + " root(s) from " + lockPath);
        }
    }

    private static void verifyTree(ArtifactLock lock, Path root) throws IOException {
        verifyOne(lock, root, false);
        for (ArtifactLock dependency : lock.dependencies()) verifyTree(dependency, root);
    }

    private static void verifyOne(ArtifactLock lock, Path root, boolean pom) throws IOException {
        Path cache = safeCachePath(root, lock.cachePath());
        Path pomCache = safeCachePath(root, lock.pomCachePath());
        byte[] artifactBytes = readCache(cache, lock.coordinate(), "artifact");
        byte[] pomBytes = readCache(pomCache, lock.coordinate(), "POM");
        String actualHash = sha256(artifactBytes);
        String actualPomHash = sha256(pomBytes);
        if (!actualHash.equalsIgnoreCase(lock.sha256())) {
            throw new LockVerificationException(LockVerificationException.Reason.SHA_MISMATCH,
                    "SHA_MISMATCH for " + lock.coordinate() + " at " + lock.cachePath());
        }
        if (!actualPomHash.equalsIgnoreCase(lock.pomHash())) {
            throw new LockVerificationException(LockVerificationException.Reason.POM_HASH_MISMATCH,
                    "POM_HASH_MISMATCH for " + lock.coordinate() + " at " + lock.pomCachePath());
        }
    }

    private static Path safeCachePath(Path root, String relative) throws IOException {
        Path path = Path.of(relative);
        if (path.isAbsolute()) throw new LockVerificationException(LockVerificationException.Reason.INVALID_LOCK,
                "Absolute cache path is forbidden: " + relative);
        Path resolved = root.resolve(path).normalize();
        if (!resolved.startsWith(root)) throw new LockVerificationException(LockVerificationException.Reason.INVALID_LOCK,
                "Cache path escapes project root: " + relative);
        if (!relative.replace('\\', '/').startsWith(".gradle/meldrite/artifacts/")) {
            throw new LockVerificationException(LockVerificationException.Reason.INVALID_LOCK,
                    "Cache path must be under .gradle/meldrite/artifacts: " + relative);
        }
        return resolved;
    }

    private static byte[] readCache(Path path, String coordinate, String kind) throws IOException {
        if (!Files.isRegularFile(path)) throw new LockVerificationException(LockVerificationException.Reason.MISSING_CACHE,
                "MISSING_CACHE for " + coordinate + " (" + kind + "): " + path);
        return Files.readAllBytes(path);
    }

    static String sha256(byte[] bytes) throws IOException {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException ex) {
            throw new IOException("SHA-256 is unavailable", ex);
        }
    }
}
