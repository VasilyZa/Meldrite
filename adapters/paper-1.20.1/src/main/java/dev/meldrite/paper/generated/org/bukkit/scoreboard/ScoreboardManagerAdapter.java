package dev.meldrite.paper.generated.org.bukkit.scoreboard;
public final class ScoreboardManagerAdapter implements org.bukkit.scoreboard.ScoreboardManager {
private final dev.meldrite.service.ScoreboardService service;private final dev.meldrite.plugin.spi.PluginScope owner;public ScoreboardManagerAdapter(dev.meldrite.service.ScoreboardService service,dev.meldrite.plugin.spi.PluginScope owner){this.service=java.util.Objects.requireNonNull(service);this.owner=java.util.Objects.requireNonNull(owner);}
  @Override public org.bukkit.scoreboard.Scoreboard getMainScoreboard() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/ScoreboardManager#getMainScoreboard()Lorg/bukkit/scoreboard/Scoreboard;#METHOD"); }
  @Override public org.bukkit.scoreboard.Scoreboard getNewScoreboard() { return new dev.meldrite.paper.scoreboard.ScoreboardView(service.create(owner)).board(); }
}
