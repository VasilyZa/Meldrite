package org.bukkit.craftbukkit.scoreboard;

/**
 * Legacy CraftBukkit owner used by plugins compiled before the versioned
 * package was introduced. The live implementation is the version-owned
 * manager; this base keeps the JVM owner and descriptor stable while still
 * dispatching to that real manager.
 */
public abstract class CraftScoreboardManager implements org.bukkit.scoreboard.ScoreboardManager {
    protected CraftScoreboardManager() { }

    /** Removes the player's board assignment from the live manager. */
    public abstract void removePlayer(org.bukkit.entity.Player player);

    /** Exact 1.20_R1 bridge for the native scoreboard handle. */
    public final void removePlayer(org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer player) {
        removePlayer((org.bukkit.entity.Player) player);
    }
}
