package dev.meldrite.v1201.bridge;

import java.util.Set;

/** Fails startup before READY when a production-critical native hook is unavailable. */
public final class HookHealthVerifier {
    public static final String BLOCK_BREAK = "block-break.pre-commit";
    private HookHealthVerifier() {}

    public static void verify(HookGateway gateway) {
        if (gateway == null || !gateway.ready()) throw new IllegalStateException("MISSING_REQUIRED_HOOK: " + BLOCK_BREAK);
    }

    public static Set<String> required() { return Set.of(BLOCK_BREAK); }
}
