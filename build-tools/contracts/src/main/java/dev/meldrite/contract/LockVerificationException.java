package dev.meldrite.contract;

import java.io.IOException;

/** A local lock verification failure with a stable machine-readable reason. */
public final class LockVerificationException extends IOException {
    public enum Reason { MISSING_CACHE, SHA_MISMATCH, POM_HASH_MISMATCH, INVALID_LOCK }

    private final Reason reason;

    public LockVerificationException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public Reason reason() { return reason; }
}
