package dev.meldrite.compat.engine;

import dev.meldrite.compat.CompatibilityContext;
import dev.meldrite.compat.CompatibilityPack;

public final class PackAudit {
    public String apply(CompatibilityPack pack, CompatibilityContext context) {
        try { pack.apply(context); pack.audit(context); return "APPLIED"; }
        catch (Exception failure) { return "TARGET_PLUGIN_FAILED"; }
    }
}
