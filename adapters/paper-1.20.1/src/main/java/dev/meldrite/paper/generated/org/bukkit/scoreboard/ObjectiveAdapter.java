package dev.meldrite.paper.generated.org.bukkit.scoreboard;
public final class ObjectiveAdapter implements org.bukkit.scoreboard.Objective {
private final dev.meldrite.paper.scoreboard.ScoreboardView view;private final dev.meldrite.service.ScoreboardService.Objective state;public ObjectiveAdapter(dev.meldrite.paper.scoreboard.ScoreboardView view,dev.meldrite.service.ScoreboardService.Objective state){this.view=java.util.Objects.requireNonNull(view);this.state=java.util.Objects.requireNonNull(state);}
  @Override public net.kyori.adventure.text.Component displayName() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/Objective#displayName()Lnet/kyori/adventure/text/Component;#METHOD"); }
  @Override public void displayName(net.kyori.adventure.text.Component p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/Objective#displayName(Lnet/kyori/adventure/text/Component;)V#METHOD"); }
  @Override public java.lang.String getCriteria() { return state.criteria(); }
  @Override public java.lang.String getDisplayName() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/Objective#getDisplayName()Ljava/lang/String;#METHOD"); }
  @Override public org.bukkit.scoreboard.DisplaySlot getDisplaySlot() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/Objective#getDisplaySlot()Lorg/bukkit/scoreboard/DisplaySlot;#METHOD"); }
  @Override public java.lang.String getName() { return state.name(); }
  @Override public org.bukkit.scoreboard.RenderType getRenderType() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/Objective#getRenderType()Lorg/bukkit/scoreboard/RenderType;#METHOD"); }
  @Override public org.bukkit.scoreboard.Score getScore(java.lang.String p0) { return new ScoreAdapter(view,state,p0); }
  @Override public org.bukkit.scoreboard.Score getScore(org.bukkit.OfflinePlayer p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/Objective#getScore(Lorg/bukkit/OfflinePlayer;)Lorg/bukkit/scoreboard/Score;#METHOD"); }
  @Override public org.bukkit.scoreboard.Score getScoreFor(org.bukkit.entity.Entity p0) throws java.lang.IllegalArgumentException, java.lang.IllegalStateException { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/Objective#getScoreFor(Lorg/bukkit/entity/Entity;)Lorg/bukkit/scoreboard/Score;#METHOD"); }
  @Override public org.bukkit.scoreboard.Scoreboard getScoreboard() { return state.attached()?view.board():null; }
  @Override public org.bukkit.scoreboard.Criteria getTrackedCriteria() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/Objective#getTrackedCriteria()Lorg/bukkit/scoreboard/Criteria;#METHOD"); }
  @Override public boolean isModifiable() { return state.modifiable(); }
  @Override public void setDisplayName(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/Objective#setDisplayName(Ljava/lang/String;)V#METHOD"); }
  @Override public void setDisplaySlot(org.bukkit.scoreboard.DisplaySlot p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/Objective#setDisplaySlot(Lorg/bukkit/scoreboard/DisplaySlot;)V#METHOD"); }
  @Override public void setRenderType(org.bukkit.scoreboard.RenderType p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/Objective#setRenderType(Lorg/bukkit/scoreboard/RenderType;)V#METHOD"); }
  @Override public void unregister() { view.remove(state); }
}
