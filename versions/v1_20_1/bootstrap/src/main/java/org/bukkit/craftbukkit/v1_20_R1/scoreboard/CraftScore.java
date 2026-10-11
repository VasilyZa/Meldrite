package org.bukkit.craftbukkit.v1_20_R1.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scoreboard.Score;

public final class CraftScore implements Score {
    private final CraftObjective objective;
    private final String entry;
    CraftScore(CraftObjective objective, String entry) { this.objective = objective; this.entry = entry; }
    @Override public OfflinePlayer getPlayer() { return Bukkit.getOfflinePlayer(entry); }
    @Override public String getEntry() { return entry; }
    @Override public CraftObjective getObjective() { return objective; }
    @Override public int getScore() {
        var board = objective.checkState().handle;
        if (!board.playerHasObjective(entry, objective.getHandle())) return 0;
        return board.getPlayerObjectives(entry).get(objective.getHandle()).getScore();
    }
    @Override public void setScore(int value) { var board = objective.checkState(); board.mutation(); board.handle.getPlayerScore(entry, objective.getHandle()).setScore(value); }
    @Override public boolean isScoreSet() { return objective.checkState().handle.playerHasObjective(entry, objective.getHandle()); }
    @Override public CraftScoreboard getScoreboard() { return objective.getScoreboard(); }
    @Override public void resetScore() { var board = objective.checkState(); board.mutation(); board.handle.resetPlayerScore(entry, objective.getHandle()); }
    @Override public boolean equals(Object other) { return other instanceof CraftScore value && objective.equals(value.objective) && entry.equals(value.entry); }
    @Override public int hashCode() { return 31 * objective.hashCode() + entry.hashCode(); }
}
