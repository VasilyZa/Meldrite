package dev.meldrite.paper1211;

/** Version-owned profile metadata; game calls stay behind this boundary. */
public final class ProfileFactory {
    private ProfileFactory() {}
    public static String profileId() { return "paper-1.21.1"; }
    public static int javaFeature() { return 21; }
}
