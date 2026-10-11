package dev.meldrite.remap;

import java.util.Objects;

public record MappingBundle(String mappingHash, String hierarchyHash, String transformerVersion) {
    public MappingBundle { Objects.requireNonNull(mappingHash); Objects.requireNonNull(hierarchyHash); Objects.requireNonNull(transformerVersion); }
}
