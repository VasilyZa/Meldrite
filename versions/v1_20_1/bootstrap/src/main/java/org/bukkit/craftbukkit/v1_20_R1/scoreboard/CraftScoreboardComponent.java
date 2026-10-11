package org.bukkit.craftbukkit.v1_20_R1.scoreboard;

/** Shared lifecycle guard for Craft scoreboard wrappers. */
abstract class CraftScoreboardComponent {
    private final CraftScoreboard scoreboard;
    CraftScoreboardComponent(CraftScoreboard scoreboard) { this.scoreboard = java.util.Objects.requireNonNull(scoreboard); }
    abstract CraftScoreboard checkState();
    public CraftScoreboard getScoreboard() { return scoreboard; }
    abstract void unregister();
}
