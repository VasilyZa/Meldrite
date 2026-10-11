package dev.meldrite.contract;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Versioned JSON envelope used by contract lock files. */
public record ArtifactLockFile(int schemaVersion, List<ArtifactLock> roots, String upstreamSourceCommit,
        String resolvedAt, String sourceCommitRelation) {
    private static final ObjectMapper JSON = new ObjectMapper();

    public ArtifactLockFile {
        roots = List.copyOf(roots);
    }

    public ArtifactLockFile(int schemaVersion, List<ArtifactLock> roots) {
        this(schemaVersion, roots, null, null, null);
    }

    public ArtifactLockFile(int schemaVersion, List<ArtifactLock> roots, String upstreamSourceCommit) {
        this(schemaVersion, roots, upstreamSourceCommit,
                upstreamSourceCommit == null ? null : java.time.Instant.now().toString(),
                upstreamSourceCommit == null ? null : "REFERENCE_ONLY");
    }

    public static ArtifactLockFile read(Path path) throws IOException {
        JsonNode json = JSON.readTree(path.toFile());
        if (json == null || json.path("schemaVersion").asInt(-1) != 1 || !json.path("roots").isArray()) {
            throw new IOException("Invalid lock file (expected schemaVersion 1 and roots array): " + path);
        }
        List<ArtifactLock> roots = new ArrayList<>();
        for (JsonNode root : json.path("roots")) roots.add(parseLock(root));
        if (roots.isEmpty()) throw new IOException("Invalid lock file: roots must not be empty");
        JsonNode provenance = json.path("provenance");
        String sourceCommit = provenance.path("upstreamSourceCommit").isTextual()
                ? provenance.path("upstreamSourceCommit").asText() : null;
        String resolvedAt = provenance.path("resolvedAt").isTextual() ? provenance.path("resolvedAt").asText() : null;
        String relation = provenance.path("sourceCommitRelation").isTextual()
                ? provenance.path("sourceCommitRelation").asText() : null;
        return new ArtifactLockFile(1, roots, sourceCommit, resolvedAt, relation);
    }

    public void write(Path path) throws IOException {
        Files.createDirectories(path.toAbsolutePath().getParent());
        JSON.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), toJson());
    }

    ObjectNode toJson() {
        ObjectNode envelope = JSON.createObjectNode().put("schemaVersion", schemaVersion);
        if (upstreamSourceCommit != null && !upstreamSourceCommit.isBlank()) {
            ObjectNode provenance = envelope.putObject("provenance").put("upstreamSourceCommit", upstreamSourceCommit);
            if (resolvedAt != null) provenance.put("resolvedAt", resolvedAt);
            if (sourceCommitRelation != null) provenance.put("sourceCommitRelation", sourceCommitRelation);
        } else if (resolvedAt != null) {
            envelope.putObject("provenance").put("resolvedAt", resolvedAt);
        }
        ArrayNode array = envelope.putArray("roots");
        roots.forEach(root -> array.add(lockJson(root)));
        return envelope;
    }

    static ObjectNode lockJson(ArtifactLock lock) {
        ObjectNode node = JSON.createObjectNode()
                .put("coordinate", lock.coordinate())
                .put("resolvedVersion", lock.resolvedVersion())
                .put("sourceUrl", lock.sourceUrl())
                .put("pomUrl", lock.pomUrl())
                .put("cachePath", lock.cachePath())
                .put("pomCachePath", lock.pomCachePath())
                .put("sha256", lock.sha256())
                .put("pomHash", lock.pomHash());
        ArrayNode dependencies = node.putArray("dependencies");
        lock.dependencies().forEach(dependency -> dependencies.add(lockJson(dependency)));
        return node;
    }

    private static ArtifactLock parseLock(JsonNode node) throws IOException {
        String[] fields = {"coordinate", "resolvedVersion", "sourceUrl", "pomUrl", "cachePath", "pomCachePath", "sha256", "pomHash"};
        for (String field : fields) if (!node.path(field).isTextual() || node.path(field).asText().isBlank()) {
            throw new IOException("Lock entry missing string field: " + field);
        }
        String sha = node.path("sha256").asText();
        String pomHash = node.path("pomHash").asText();
        if (!sha.matches("(?i)[a-f0-9]{64}") || !pomHash.matches("(?i)[a-f0-9]{64}")) {
            throw new IOException("Lock entry has missing or invalid content hash for " + node.path("coordinate").asText());
        }
        if (!node.path("dependencies").isArray()) throw new IOException("Lock entry dependencies must be an array");
        List<ArtifactLock> dependencies = new ArrayList<>();
        for (JsonNode dependency : node.path("dependencies")) dependencies.add(parseLock(dependency));
        return new ArtifactLock(node.path("coordinate").asText(), node.path("resolvedVersion").asText(),
                node.path("sourceUrl").asText(), node.path("pomUrl").asText(), node.path("cachePath").asText(),
                node.path("pomCachePath").asText(), node.path("sha256").asText(), node.path("pomHash").asText(), dependencies);
    }
}
