package dev.meldrite.paper.command;
public final class CommandSenderAdapter extends dev.meldrite.paper.adventure.AudienceAdapter implements org.bukkit.command.CommandSender {
  private final dev.meldrite.command.CommandSource source;private final java.util.function.Supplier<org.bukkit.Server> server;private final dev.meldrite.adventure.ComponentCodec codec=new dev.meldrite.adventure.ComponentCodec();
  public CommandSenderAdapter(dev.meldrite.command.CommandSource source,dev.meldrite.adventure.AudienceService audiences,java.util.function.Supplier<org.bukkit.Server> server){super(audiences,source.audience());this.source=java.util.Objects.requireNonNull(source);this.server=java.util.Objects.requireNonNull(server);}
  public dev.meldrite.command.CommandSource source(){return source;}
  private void legacy(String text){sendMessage(codec.fromLegacy(java.util.Objects.requireNonNull(text)));}
  private void legacy(String[] text){for(String message:java.util.Objects.requireNonNull(text))legacy(message);}
  @Override public org.bukkit.permissions.PermissionAttachment addAttachment(org.bukkit.plugin.Plugin p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/command/CommandSender#addAttachment(Lorg/bukkit/plugin/Plugin;)Lorg/bukkit/permissions/PermissionAttachment;#INHERITED_METHOD"); }
  @Override public org.bukkit.permissions.PermissionAttachment addAttachment(org.bukkit.plugin.Plugin p0, int p1) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/command/CommandSender#addAttachment(Lorg/bukkit/plugin/Plugin;I)Lorg/bukkit/permissions/PermissionAttachment;#INHERITED_METHOD"); }
  @Override public org.bukkit.permissions.PermissionAttachment addAttachment(org.bukkit.plugin.Plugin p0, java.lang.String p1, boolean p2) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/command/CommandSender#addAttachment(Lorg/bukkit/plugin/Plugin;Ljava/lang/String;Z)Lorg/bukkit/permissions/PermissionAttachment;#INHERITED_METHOD"); }
  @Override public org.bukkit.permissions.PermissionAttachment addAttachment(org.bukkit.plugin.Plugin p0, java.lang.String p1, boolean p2, int p3) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/command/CommandSender#addAttachment(Lorg/bukkit/plugin/Plugin;Ljava/lang/String;ZI)Lorg/bukkit/permissions/PermissionAttachment;#INHERITED_METHOD"); }
  @Override public java.util.Set<org.bukkit.permissions.PermissionAttachmentInfo> getEffectivePermissions() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/command/CommandSender#getEffectivePermissions()Ljava/util/Set;#INHERITED_METHOD"); }
  @Override public java.lang.String getName() { return source.identity(); }
  @Override public org.bukkit.Server getServer() { return java.util.Objects.requireNonNull(server.get()); }
  @Override public boolean hasPermission(java.lang.String p0) { return source.permissions().test(java.util.Objects.requireNonNull(p0)); }
  @Override public boolean hasPermission(org.bukkit.permissions.Permission p0) { return source.permissions().test(java.util.Objects.requireNonNull(p0).getName()); }
  @Override public boolean isOp() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/command/CommandSender#isOp()Z#INHERITED_METHOD"); }
  @Override public boolean isPermissionSet(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/command/CommandSender#isPermissionSet(Ljava/lang/String;)Z#INHERITED_METHOD"); }
  @Override public boolean isPermissionSet(org.bukkit.permissions.Permission p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/command/CommandSender#isPermissionSet(Lorg/bukkit/permissions/Permission;)Z#INHERITED_METHOD"); }
  @Override public net.kyori.adventure.text.Component name() { return net.kyori.adventure.text.Component.text(source.identity()); }
  @Override public void recalculatePermissions() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/command/CommandSender#recalculatePermissions()V#INHERITED_METHOD"); }
  @Override public void removeAttachment(org.bukkit.permissions.PermissionAttachment p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/command/CommandSender#removeAttachment(Lorg/bukkit/permissions/PermissionAttachment;)V#INHERITED_METHOD"); }
  @Override public void sendMessage(java.lang.String p0) { legacy(p0); }
  @Override public void sendMessage(java.util.UUID p0, java.lang.String p1) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/command/CommandSender#sendMessage(Ljava/util/UUID;Ljava/lang/String;)V#METHOD"); }
  @Override public void sendMessage(java.util.UUID p0, java.lang.String... p1) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/command/CommandSender#sendMessage(Ljava/util/UUID;[Ljava/lang/String;)V#METHOD"); }
  @Override public void sendMessage(java.lang.String... p0) { legacy(p0); }
  @Override public void setOp(boolean p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/command/CommandSender#setOp(Z)V#INHERITED_METHOD"); }
  @Override public org.bukkit.command.CommandSender.Spigot spigot() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/command/CommandSender#spigot()Lorg/bukkit/command/CommandSender$Spigot;#METHOD"); }
}
