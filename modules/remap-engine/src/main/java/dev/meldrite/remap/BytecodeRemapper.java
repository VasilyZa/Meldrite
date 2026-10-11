package dev.meldrite.remap;

import java.util.Arrays;

public final class BytecodeRemapper {
    public record Result(byte[] bytes, boolean transformed, String status) { public Result { bytes = bytes.clone(); } @Override public byte[] bytes(){return bytes.clone();} }
    public Result transform(PluginRequirements requirements, byte[] input) {
        if (requirements.signed()) throw new UnsupportedOperationException("SIGNED_ARTIFACT_TRANSFORM_REJECTED");
        if (requirements.apiOnly()) return new Result(input, false, "API_ONLY");
        return new Result(Arrays.copyOf(input, input.length), true, "REMAP_PLANNED");
    }
}
