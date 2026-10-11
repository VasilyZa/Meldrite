package dev.meldrite.contract;

import java.util.List;

/** Content hashes and source locations for one Maven artifact and its recursive dependencies. */
public record ArtifactLock(
        String coordinate,
        String resolvedVersion,
        String sourceUrl,
        String pomUrl,
        String cachePath,
        String pomCachePath,
        String sha256,
        String pomHash,
        List<ArtifactLock> dependencies) {
    public ArtifactLock {
        dependencies = List.copyOf(dependencies);
    }
}
