package dev.meldrite.paper.facade;
public class EntityAdapter implements org.bukkit.entity.Entity {
  private io.papermc.paper.threadedregions.scheduler.EntityScheduler entityScheduler;
  private boolean schedulerRetired;
  protected boolean meldrite$schedulerPresent(){return delegate.valid();}
  public void meldrite$retireScheduler(){io.papermc.paper.threadedregions.scheduler.EntityScheduler scheduler;synchronized(this){schedulerRetired=true;scheduler=entityScheduler;}if(scheduler!=null)context.retireEntityScheduler(scheduler);}
  protected final dev.meldrite.craft.EntityDelegate delegate; protected final FacadeContext context; public EntityAdapter(dev.meldrite.craft.EntityDelegate delegate,FacadeContext context){this.delegate=java.util.Objects.requireNonNull(delegate);this.context=java.util.Objects.requireNonNull(context);}
  @Override public org.bukkit.permissions.PermissionAttachment addAttachment(org.bukkit.plugin.Plugin p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#addAttachment(Lorg/bukkit/plugin/Plugin;)Lorg/bukkit/permissions/PermissionAttachment;#INHERITED_METHOD"); }
  @Override public org.bukkit.permissions.PermissionAttachment addAttachment(org.bukkit.plugin.Plugin p0, int p1) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#addAttachment(Lorg/bukkit/plugin/Plugin;I)Lorg/bukkit/permissions/PermissionAttachment;#INHERITED_METHOD"); }
  @Override public org.bukkit.permissions.PermissionAttachment addAttachment(org.bukkit.plugin.Plugin p0, java.lang.String p1, boolean p2) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#addAttachment(Lorg/bukkit/plugin/Plugin;Ljava/lang/String;Z)Lorg/bukkit/permissions/PermissionAttachment;#INHERITED_METHOD"); }
  @Override public org.bukkit.permissions.PermissionAttachment addAttachment(org.bukkit.plugin.Plugin p0, java.lang.String p1, boolean p2, int p3) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#addAttachment(Lorg/bukkit/plugin/Plugin;Ljava/lang/String;ZI)Lorg/bukkit/permissions/PermissionAttachment;#INHERITED_METHOD"); }
  @Override public boolean addPassenger(org.bukkit.entity.Entity p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#addPassenger(Lorg/bukkit/entity/Entity;)Z#METHOD"); }
  @Override public boolean addScoreboardTag(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#addScoreboardTag(Ljava/lang/String;)Z#METHOD"); }
  @Override public boolean collidesAt(org.bukkit.Location p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#collidesAt(Lorg/bukkit/Location;)Z#METHOD"); }
  @Override public net.kyori.adventure.text.Component customName() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#customName()Lnet/kyori/adventure/text/Component;#INHERITED_METHOD"); }
  @Override public void customName(net.kyori.adventure.text.Component p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#customName(Lnet/kyori/adventure/text/Component;)V#INHERITED_METHOD"); }
  @Override public boolean eject() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#eject()Z#METHOD"); }
  @Override public boolean fromMobSpawner() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#fromMobSpawner()Z#METHOD"); }
  @Override public org.bukkit.util.BoundingBox getBoundingBox() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#getBoundingBox()Lorg/bukkit/util/BoundingBox;#METHOD"); }
  @Override public java.lang.String getCustomName() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#getCustomName()Ljava/lang/String;#INHERITED_METHOD"); }
  @Override public java.util.Set<org.bukkit.permissions.PermissionAttachmentInfo> getEffectivePermissions() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#getEffectivePermissions()Ljava/util/Set;#INHERITED_METHOD"); }
  @Override public int getEntityId() { return delegate.identity().nativeId(); }
  @Override public org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason getEntitySpawnReason() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#getEntitySpawnReason()Lorg/bukkit/event/entity/CreatureSpawnEvent$SpawnReason;#METHOD"); }
  @Override public org.bukkit.block.BlockFace getFacing() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#getFacing()Lorg/bukkit/block/BlockFace;#METHOD"); }
  @Override public float getFallDistance() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#getFallDistance()F#METHOD"); }
  @Override public int getFireTicks() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#getFireTicks()I#METHOD"); }
  @Override public int getFreezeTicks() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#getFreezeTicks()I#METHOD"); }
  @Override public double getHeight() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#getHeight()D#METHOD"); }
  @Override public org.bukkit.event.entity.EntityDamageEvent getLastDamageCause() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#getLastDamageCause()Lorg/bukkit/event/entity/EntityDamageEvent;#METHOD"); }
  @Override public org.bukkit.Location getLocation() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#getLocation()Lorg/bukkit/Location;#METHOD"); }
  @Override public org.bukkit.Location getLocation(org.bukkit.Location p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#getLocation(Lorg/bukkit/Location;)Lorg/bukkit/Location;#METHOD"); }
  @Override public int getMaxFireTicks() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#getMaxFireTicks()I#METHOD"); }
  @Override public int getMaxFreezeTicks() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#getMaxFreezeTicks()I#METHOD"); }
  @Override public java.util.List<org.bukkit.metadata.MetadataValue> getMetadata(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#getMetadata(Ljava/lang/String;)Ljava/util/List;#INHERITED_METHOD"); }
  @Override public java.lang.String getName() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#getName()Ljava/lang/String;#INHERITED_METHOD"); }
  @Override public java.util.List<org.bukkit.entity.Entity> getNearbyEntities(double p0, double p1, double p2) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#getNearbyEntities(DDD)Ljava/util/List;#METHOD"); }
  @Override public org.bukkit.Location getOrigin() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#getOrigin()Lorg/bukkit/Location;#METHOD"); }
  @Override public org.bukkit.entity.Entity getPassenger() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#getPassenger()Lorg/bukkit/entity/Entity;#METHOD"); }
  @Override public java.util.List<org.bukkit.entity.Entity> getPassengers() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#getPassengers()Ljava/util/List;#METHOD"); }
  @Override public org.bukkit.persistence.PersistentDataContainer getPersistentDataContainer() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#getPersistentDataContainer()Lorg/bukkit/persistence/PersistentDataContainer;#INHERITED_METHOD"); }
  @Override public org.bukkit.block.PistonMoveReaction getPistonMoveReaction() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#getPistonMoveReaction()Lorg/bukkit/block/PistonMoveReaction;#METHOD"); }
  @Override public float getPitch() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#getPitch()F#METHOD"); }
  @Override public int getPortalCooldown() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#getPortalCooldown()I#METHOD"); }
  @Override public org.bukkit.entity.Pose getPose() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#getPose()Lorg/bukkit/entity/Pose;#METHOD"); }
  @Override public synchronized io.papermc.paper.threadedregions.scheduler.EntityScheduler getScheduler() { if(entityScheduler==null)entityScheduler=context.entityScheduler(this::meldrite$schedulerPresent,schedulerRetired);return entityScheduler; }
  @Override public java.lang.String getScoreboardEntryName() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#getScoreboardEntryName()Ljava/lang/String;#METHOD"); }
  @Override public java.util.Set<java.lang.String> getScoreboardTags() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#getScoreboardTags()Ljava/util/Set;#METHOD"); }
  @Override public org.bukkit.Server getServer() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#getServer()Lorg/bukkit/Server;#METHOD"); }
  @Override public org.bukkit.entity.SpawnCategory getSpawnCategory() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#getSpawnCategory()Lorg/bukkit/entity/SpawnCategory;#METHOD"); }
  @Override public org.bukkit.Sound getSwimHighSpeedSplashSound() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#getSwimHighSpeedSplashSound()Lorg/bukkit/Sound;#METHOD"); }
  @Override public org.bukkit.Sound getSwimSound() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#getSwimSound()Lorg/bukkit/Sound;#METHOD"); }
  @Override public org.bukkit.Sound getSwimSplashSound() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#getSwimSplashSound()Lorg/bukkit/Sound;#METHOD"); }
  @Override public int getTicksLived() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#getTicksLived()I#METHOD"); }
  @Override public java.util.Set<org.bukkit.entity.Player> getTrackedPlayers() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#getTrackedPlayers()Ljava/util/Set;#METHOD"); }
  @Override public org.bukkit.entity.EntityType getType() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#getType()Lorg/bukkit/entity/EntityType;#METHOD"); }
  @Override public java.util.UUID getUniqueId() { return delegate.identity().handle().uuid(); }
  @Override public org.bukkit.entity.Entity getVehicle() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#getVehicle()Lorg/bukkit/entity/Entity;#METHOD"); }
  @Override public org.bukkit.util.Vector getVelocity() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#getVelocity()Lorg/bukkit/util/Vector;#METHOD"); }
  @Override public double getWidth() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#getWidth()D#METHOD"); }
  @Override public org.bukkit.World getWorld() { return context.world(delegate.identity().world()); }
  @Override public double getX() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#getX()D#METHOD"); }
  @Override public double getY() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#getY()D#METHOD"); }
  @Override public float getYaw() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#getYaw()F#METHOD"); }
  @Override public double getZ() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#getZ()D#METHOD"); }
  @Override public boolean hasFixedPose() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#hasFixedPose()Z#METHOD"); }
  @Override public boolean hasGravity() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#hasGravity()Z#METHOD"); }
  @Override public boolean hasMetadata(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#hasMetadata(Ljava/lang/String;)Z#INHERITED_METHOD"); }
  @Override public boolean hasPermission(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#hasPermission(Ljava/lang/String;)Z#INHERITED_METHOD"); }
  @Override public boolean hasPermission(org.bukkit.permissions.Permission p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#hasPermission(Lorg/bukkit/permissions/Permission;)Z#INHERITED_METHOD"); }
  @Override public boolean isCustomNameVisible() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#isCustomNameVisible()Z#METHOD"); }
  @Override public boolean isDead() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#isDead()Z#METHOD"); }
  @Override public boolean isEmpty() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#isEmpty()Z#METHOD"); }
  @Override public boolean isFreezeTickingLocked() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#isFreezeTickingLocked()Z#METHOD"); }
  @Override public boolean isFrozen() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#isFrozen()Z#METHOD"); }
  @Override public boolean isGlowing() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#isGlowing()Z#METHOD"); }
  @Override public boolean isInBubbleColumn() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#isInBubbleColumn()Z#METHOD"); }
  @Override public boolean isInLava() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#isInLava()Z#METHOD"); }
  @Override public boolean isInPowderedSnow() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#isInPowderedSnow()Z#METHOD"); }
  @Override public boolean isInRain() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#isInRain()Z#METHOD"); }
  @Override public boolean isInWater() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#isInWater()Z#METHOD"); }
  @Override public boolean isInWaterOrBubbleColumn() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#isInWaterOrBubbleColumn()Z#METHOD"); }
  @Override public boolean isInWaterOrRain() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#isInWaterOrRain()Z#METHOD"); }
  @Override public boolean isInWaterOrRainOrBubbleColumn() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#isInWaterOrRainOrBubbleColumn()Z#METHOD"); }
  @Override public boolean isInsideVehicle() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#isInsideVehicle()Z#METHOD"); }
  @Override public boolean isInvulnerable() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#isInvulnerable()Z#METHOD"); }
  @Override public boolean isOnGround() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#isOnGround()Z#METHOD"); }
  @Override public boolean isOp() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#isOp()Z#INHERITED_METHOD"); }
  @Override public boolean isPermissionSet(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#isPermissionSet(Ljava/lang/String;)Z#INHERITED_METHOD"); }
  @Override public boolean isPermissionSet(org.bukkit.permissions.Permission p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#isPermissionSet(Lorg/bukkit/permissions/Permission;)Z#INHERITED_METHOD"); }
  @Override public boolean isPersistent() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#isPersistent()Z#METHOD"); }
  @Override public boolean isSilent() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#isSilent()Z#METHOD"); }
  @Override public boolean isSneaking() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#isSneaking()Z#METHOD"); }
  @Override public boolean isTicking() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#isTicking()Z#METHOD"); }
  @Override public boolean isUnderWater() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#isUnderWater()Z#METHOD"); }
  @Override public boolean isValid() { return delegate.valid(); }
  @Override public boolean isVisibleByDefault() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#isVisibleByDefault()Z#METHOD"); }
  @Override public boolean isVisualFire() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#isVisualFire()Z#METHOD"); }
  @Override public boolean leaveVehicle() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#leaveVehicle()Z#METHOD"); }
  @Override public void lockFreezeTicks(boolean p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#lockFreezeTicks(Z)V#METHOD"); }
  @Override public net.kyori.adventure.text.Component name() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#name()Lnet/kyori/adventure/text/Component;#INHERITED_METHOD"); }
  @Override public void playEffect(org.bukkit.EntityEffect p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#playEffect(Lorg/bukkit/EntityEffect;)V#METHOD"); }
  @Override public void recalculatePermissions() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#recalculatePermissions()V#INHERITED_METHOD"); }
  @Override public void remove() { context.remove(delegate); }
  @Override public void removeAttachment(org.bukkit.permissions.PermissionAttachment p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#removeAttachment(Lorg/bukkit/permissions/PermissionAttachment;)V#INHERITED_METHOD"); }
  @Override public void removeMetadata(java.lang.String p0, org.bukkit.plugin.Plugin p1) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#removeMetadata(Ljava/lang/String;Lorg/bukkit/plugin/Plugin;)V#INHERITED_METHOD"); }
  @Override public boolean removePassenger(org.bukkit.entity.Entity p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#removePassenger(Lorg/bukkit/entity/Entity;)Z#METHOD"); }
  @Override public boolean removeScoreboardTag(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#removeScoreboardTag(Ljava/lang/String;)Z#METHOD"); }
  @Override public void sendMessage(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#sendMessage(Ljava/lang/String;)V#INHERITED_METHOD"); }
  @Override public void sendMessage(java.util.UUID p0, java.lang.String p1) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#sendMessage(Ljava/util/UUID;Ljava/lang/String;)V#INHERITED_METHOD"); }
  @Override public void sendMessage(java.util.UUID p0, java.lang.String... p1) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#sendMessage(Ljava/util/UUID;[Ljava/lang/String;)V#INHERITED_METHOD"); }
  @Override public void sendMessage(java.lang.String... p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#sendMessage([Ljava/lang/String;)V#INHERITED_METHOD"); }
  @Override public void setCustomName(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#setCustomName(Ljava/lang/String;)V#INHERITED_METHOD"); }
  @Override public void setCustomNameVisible(boolean p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#setCustomNameVisible(Z)V#METHOD"); }
  @Override public void setFallDistance(float p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#setFallDistance(F)V#METHOD"); }
  @Override public void setFireTicks(int p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#setFireTicks(I)V#METHOD"); }
  @Override public void setFreezeTicks(int p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#setFreezeTicks(I)V#METHOD"); }
  @Override public void setGlowing(boolean p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#setGlowing(Z)V#METHOD"); }
  @Override public void setGravity(boolean p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#setGravity(Z)V#METHOD"); }
  @Override public void setInvulnerable(boolean p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#setInvulnerable(Z)V#METHOD"); }
  @Override public void setLastDamageCause(org.bukkit.event.entity.EntityDamageEvent p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#setLastDamageCause(Lorg/bukkit/event/entity/EntityDamageEvent;)V#METHOD"); }
  @Override public void setMetadata(java.lang.String p0, org.bukkit.metadata.MetadataValue p1) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#setMetadata(Ljava/lang/String;Lorg/bukkit/metadata/MetadataValue;)V#INHERITED_METHOD"); }
  @Override public void setOp(boolean p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#setOp(Z)V#INHERITED_METHOD"); }
  @Override public boolean setPassenger(org.bukkit.entity.Entity p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#setPassenger(Lorg/bukkit/entity/Entity;)Z#METHOD"); }
  @Override public void setPersistent(boolean p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#setPersistent(Z)V#METHOD"); }
  @Override public void setPortalCooldown(int p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#setPortalCooldown(I)V#METHOD"); }
  @Override public void setPose(org.bukkit.entity.Pose p0, boolean p1) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#setPose(Lorg/bukkit/entity/Pose;Z)V#METHOD"); }
  @Override public void setRotation(float p0, float p1) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#setRotation(FF)V#METHOD"); }
  @Override public void setSilent(boolean p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#setSilent(Z)V#METHOD"); }
  @Override public void setSneaking(boolean p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#setSneaking(Z)V#METHOD"); }
  @Override public void setTicksLived(int p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#setTicksLived(I)V#METHOD"); }
  @Override public void setVelocity(org.bukkit.util.Vector p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#setVelocity(Lorg/bukkit/util/Vector;)V#METHOD"); }
  @Override public void setVisibleByDefault(boolean p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#setVisibleByDefault(Z)V#METHOD"); }
  @Override public void setVisualFire(boolean p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#setVisualFire(Z)V#METHOD"); }
  @Override public boolean spawnAt(org.bukkit.Location p0, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason p1) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#spawnAt(Lorg/bukkit/Location;Lorg/bukkit/event/entity/CreatureSpawnEvent$SpawnReason;)Z#METHOD"); }
  @Override public org.bukkit.entity.Entity.Spigot spigot() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#spigot()Lorg/bukkit/entity/Entity$Spigot;#METHOD"); }
  @Override public net.kyori.adventure.text.Component teamDisplayName() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#teamDisplayName()Lnet/kyori/adventure/text/Component;#METHOD"); }
  @Override public boolean teleport(org.bukkit.Location p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#teleport(Lorg/bukkit/Location;)Z#METHOD"); }
  @Override public boolean teleport(org.bukkit.Location p0, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause p1) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#teleport(Lorg/bukkit/Location;Lorg/bukkit/event/player/PlayerTeleportEvent$TeleportCause;)Z#METHOD"); }
  @Override public boolean teleport(org.bukkit.Location p0, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause p1, io.papermc.paper.entity.TeleportFlag... p2) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#teleport(Lorg/bukkit/Location;Lorg/bukkit/event/player/PlayerTeleportEvent$TeleportCause;[Lio/papermc/paper/entity/TeleportFlag;)Z#METHOD"); }
  @Override public boolean teleport(org.bukkit.entity.Entity p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#teleport(Lorg/bukkit/entity/Entity;)Z#METHOD"); }
  @Override public boolean teleport(org.bukkit.entity.Entity p0, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause p1) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#teleport(Lorg/bukkit/entity/Entity;Lorg/bukkit/event/player/PlayerTeleportEvent$TeleportCause;)Z#METHOD"); }
  @Override public boolean wouldCollideUsing(org.bukkit.util.BoundingBox p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/entity/Entity#wouldCollideUsing(Lorg/bukkit/util/BoundingBox;)Z#METHOD"); }
}
