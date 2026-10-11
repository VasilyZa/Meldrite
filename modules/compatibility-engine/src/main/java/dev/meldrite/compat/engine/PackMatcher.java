package dev.meldrite.compat.engine;

import dev.meldrite.compat.CompatibilityContext;
import dev.meldrite.compat.CompatibilityPack;

public final class PackMatcher {
    public enum Result { MATCH, NOT_APPLICABLE, SKIPPED }
    public Result match(CompatibilityPack pack, CompatibilityContext context) {
        pack.validate();
        if (!pack.enabled()) return Result.SKIPPED;
        if (!pack.minecraftVersion().equals(context.minecraftVersion()) || !pack.profile().equals(context.profile()) || !pack.targetJarHash().equals(context.jarHash())) return Result.NOT_APPLICABLE;
        return Result.MATCH;
    }
}
