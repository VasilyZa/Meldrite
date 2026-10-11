package dev.meldrite.paper.generated.org.bukkit.scoreboard;
public final class TeamAdapter implements org.bukkit.scoreboard.Team {
private final dev.meldrite.paper.scoreboard.ScoreboardView view;private final dev.meldrite.service.ScoreboardService.Team state;public TeamAdapter(dev.meldrite.paper.scoreboard.ScoreboardView view,dev.meldrite.service.ScoreboardService.Team state){this.view=java.util.Objects.requireNonNull(view);this.state=java.util.Objects.requireNonNull(state);}
  @Override public void addEntities(java.util.Collection<org.bukkit.entity.Entity> p0) throws java.lang.IllegalStateException, java.lang.IllegalArgumentException { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/Team#addEntities(Ljava/util/Collection;)V#METHOD"); }
  @Override public void addEntity(org.bukkit.entity.Entity p0) throws java.lang.IllegalStateException, java.lang.IllegalArgumentException { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/Team#addEntity(Lorg/bukkit/entity/Entity;)V#METHOD"); }
  @Override public void addEntries(java.util.Collection<java.lang.String> p0) throws java.lang.IllegalStateException, java.lang.IllegalArgumentException { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/Team#addEntries(Ljava/util/Collection;)V#METHOD"); }
  @Override public void addEntry(java.lang.String p0) { state.add(p0); }
  @Override public void addPlayer(org.bukkit.OfflinePlayer p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/Team#addPlayer(Lorg/bukkit/OfflinePlayer;)V#METHOD"); }
  @Override public boolean allowFriendlyFire() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/Team#allowFriendlyFire()Z#METHOD"); }
  @Override public boolean canSeeFriendlyInvisibles() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/Team#canSeeFriendlyInvisibles()Z#METHOD"); }
  @Override public net.kyori.adventure.text.format.TextColor color() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/Team#color()Lnet/kyori/adventure/text/format/TextColor;#METHOD"); }
  @Override public void color(net.kyori.adventure.text.format.NamedTextColor p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/Team#color(Lnet/kyori/adventure/text/format/NamedTextColor;)V#METHOD"); }
  @Override public net.kyori.adventure.text.Component displayName() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/Team#displayName()Lnet/kyori/adventure/text/Component;#METHOD"); }
  @Override public void displayName(net.kyori.adventure.text.Component p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/Team#displayName(Lnet/kyori/adventure/text/Component;)V#METHOD"); }
  @Override public org.bukkit.ChatColor getColor() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/Team#getColor()Lorg/bukkit/ChatColor;#METHOD"); }
  @Override public java.lang.String getDisplayName() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/Team#getDisplayName()Ljava/lang/String;#METHOD"); }
  @Override public java.util.Set<java.lang.String> getEntries() { return state.entries(); }
  @Override public java.lang.String getName() { return state.name(); }
  @Override public org.bukkit.scoreboard.NameTagVisibility getNameTagVisibility() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/Team#getNameTagVisibility()Lorg/bukkit/scoreboard/NameTagVisibility;#METHOD"); }
  @Override public org.bukkit.scoreboard.Team.OptionStatus getOption(org.bukkit.scoreboard.Team.Option p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/Team#getOption(Lorg/bukkit/scoreboard/Team$Option;)Lorg/bukkit/scoreboard/Team$OptionStatus;#METHOD"); }
  @Override public java.util.Set<org.bukkit.OfflinePlayer> getPlayers() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/Team#getPlayers()Ljava/util/Set;#METHOD"); }
  @Override public java.lang.String getPrefix() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/Team#getPrefix()Ljava/lang/String;#METHOD"); }
  @Override public org.bukkit.scoreboard.Scoreboard getScoreboard() { return state.attached()?view.board():null; }
  @Override public int getSize() { return state.entries().size(); }
  @Override public java.lang.String getSuffix() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/Team#getSuffix()Ljava/lang/String;#METHOD"); }
  @Override public boolean hasColor() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/Team#hasColor()Z#METHOD"); }
  @Override public boolean hasEntity(org.bukkit.entity.Entity p0) throws java.lang.IllegalStateException, java.lang.IllegalArgumentException { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/Team#hasEntity(Lorg/bukkit/entity/Entity;)Z#METHOD"); }
  @Override public boolean hasEntry(java.lang.String p0) { return state.has(p0); }
  @Override public boolean hasPlayer(org.bukkit.OfflinePlayer p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/Team#hasPlayer(Lorg/bukkit/OfflinePlayer;)Z#METHOD"); }
  @Override public net.kyori.adventure.text.Component prefix() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/Team#prefix()Lnet/kyori/adventure/text/Component;#METHOD"); }
  @Override public void prefix(net.kyori.adventure.text.Component p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/Team#prefix(Lnet/kyori/adventure/text/Component;)V#METHOD"); }
  @Override public boolean removeEntities(java.util.Collection<org.bukkit.entity.Entity> p0) throws java.lang.IllegalStateException, java.lang.IllegalArgumentException { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/Team#removeEntities(Ljava/util/Collection;)Z#METHOD"); }
  @Override public boolean removeEntity(org.bukkit.entity.Entity p0) throws java.lang.IllegalStateException, java.lang.IllegalArgumentException { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/Team#removeEntity(Lorg/bukkit/entity/Entity;)Z#METHOD"); }
  @Override public boolean removeEntries(java.util.Collection<java.lang.String> p0) throws java.lang.IllegalStateException, java.lang.IllegalArgumentException { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/Team#removeEntries(Ljava/util/Collection;)Z#METHOD"); }
  @Override public boolean removeEntry(java.lang.String p0) { return state.remove(p0); }
  @Override public boolean removePlayer(org.bukkit.OfflinePlayer p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/Team#removePlayer(Lorg/bukkit/OfflinePlayer;)Z#METHOD"); }
  @Override public void setAllowFriendlyFire(boolean p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/Team#setAllowFriendlyFire(Z)V#METHOD"); }
  @Override public void setCanSeeFriendlyInvisibles(boolean p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/Team#setCanSeeFriendlyInvisibles(Z)V#METHOD"); }
  @Override public void setColor(org.bukkit.ChatColor p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/Team#setColor(Lorg/bukkit/ChatColor;)V#METHOD"); }
  @Override public void setDisplayName(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/Team#setDisplayName(Ljava/lang/String;)V#METHOD"); }
  @Override public void setNameTagVisibility(org.bukkit.scoreboard.NameTagVisibility p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/Team#setNameTagVisibility(Lorg/bukkit/scoreboard/NameTagVisibility;)V#METHOD"); }
  @Override public void setOption(org.bukkit.scoreboard.Team.Option p0, org.bukkit.scoreboard.Team.OptionStatus p1) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/Team#setOption(Lorg/bukkit/scoreboard/Team$Option;Lorg/bukkit/scoreboard/Team$OptionStatus;)V#METHOD"); }
  @Override public void setPrefix(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/Team#setPrefix(Ljava/lang/String;)V#METHOD"); }
  @Override public void setSuffix(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/Team#setSuffix(Ljava/lang/String;)V#METHOD"); }
  @Override public net.kyori.adventure.text.Component suffix() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/Team#suffix()Lnet/kyori/adventure/text/Component;#METHOD"); }
  @Override public void suffix(net.kyori.adventure.text.Component p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/scoreboard/Team#suffix(Lnet/kyori/adventure/text/Component;)V#METHOD"); }
  @Override public void unregister() { view.remove(state); }
}
