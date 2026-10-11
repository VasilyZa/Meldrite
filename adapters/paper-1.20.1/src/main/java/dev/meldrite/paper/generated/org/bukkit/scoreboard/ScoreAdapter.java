package dev.meldrite.paper.generated.org.bukkit.scoreboard;
public final class ScoreAdapter implements org.bukkit.scoreboard.Score {
private final dev.meldrite.paper.scoreboard.ScoreboardView view;private final dev.meldrite.service.ScoreboardService.Objective state;private final String entry;public ScoreAdapter(dev.meldrite.paper.scoreboard.ScoreboardView view,dev.meldrite.service.ScoreboardService.Objective state,String entry){this.view=java.util.Objects.requireNonNull(view);this.state=java.util.Objects.requireNonNull(state);this.entry=java.util.Objects.requireNonNull(entry);state.score(entry);}
  @Override public java.lang.String getEntry() { return entry; }
  @Override public org.bukkit.scoreboard.Objective getObjective() { return view.objective(state); }
  @Override public org.bukkit.OfflinePlayer getPlayer() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/Score#getPlayer()Lorg/bukkit/OfflinePlayer;#METHOD"); }
  @Override public int getScore() { return state.score(entry); }
  @Override public org.bukkit.scoreboard.Scoreboard getScoreboard() { return state.attached()?view.board():null; }
  @Override public boolean isScoreSet() { return state.scoreSet(entry); }
  @Override public void resetScore() throws java.lang.IllegalStateException { state.reset(entry); }
  @Override public void setScore(int p0) { state.score(entry,p0); }
}
