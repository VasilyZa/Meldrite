package org.bukkit.craftbukkit;

import dev.meldrite.paper.bootstrap.ProfileBootstrap;
import dev.meldrite.paper.facade.FacadeContext;

/**
 * Shared owner for the legacy CraftServer scoreboard return descriptor.
 * The concrete version class overrides this covariantly, so javac emits the
 * exact base-return bridge while retaining the version-owned return type.
 */
public abstract class CraftServerBase extends ProfileBootstrap.ProfileServer {
    protected CraftServerBase(FacadeContext context, ProfileBootstrap runtime) {
        super(context, runtime);
    }

    public abstract org.bukkit.craftbukkit.scoreboard.CraftScoreboardManager getScoreboardManager();
}
