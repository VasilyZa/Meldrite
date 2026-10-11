package dev.meldrite.remap;

public final class JarSignaturePolicy {
    public enum Decision { ALLOW, SIGNED_ARTIFACT_TRANSFORM_REJECTED }

    public Decision decide(boolean signed, boolean transformRequested) {
        return signed && transformRequested ? Decision.SIGNED_ARTIFACT_TRANSFORM_REJECTED : Decision.ALLOW;
    }
}
