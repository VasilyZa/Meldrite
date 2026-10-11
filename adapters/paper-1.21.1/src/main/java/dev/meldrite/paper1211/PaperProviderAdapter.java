package dev.meldrite.paper1211;

public final class PaperProviderAdapter {
    public String profile() { return ProfileFactory.profileId(); }
    public boolean pluginBootstrapIsProfileOwned() { return true; }
}
