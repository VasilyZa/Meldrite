package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import org.bukkit.craftbukkit.v1_20_R1.persistence.CraftPersistentDataContainer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

public class CraftEntity extends dev.meldrite.paper.facade.EntityAdapter implements org.bukkit.projectiles.ProjectileSource {
    @Override public boolean isVisibleByDefault() { return dev.meldrite.v1201.compat.NativeEntityExtensions.state(getHandle()).meldrite$isVisibleByDefault(); }
    @Override public void setVisibleByDefault(boolean visible) {
        delegate.requireWritable();
        if (isVisibleByDefault() == visible) return;
        var handle = getHandle();
        dev.meldrite.v1201.compat.NativeEntityExtensions.state(handle).meldrite$setVisibleByDefault(visible);
        if (handle.getServer() != null) for (var viewer : handle.getServer().getPlayerManager().getPlayerList()) {
            var playerState = ((dev.meldrite.v1201.compat.PaperPlayerAccess) viewer).meldrite$playerState();
            boolean previous = playerState.canSee(handle.getUuid(), !visible);
            playerState.resetVisibility(handle.getUuid());
            if (viewer == handle || previous == visible) continue;
            dev.meldrite.v1201.compat.NativePlayerVisibility.update(viewer, handle, dev.meldrite.v1201.compat.NativePlayerVisibility.canSee(viewer, handle));
            var facade = (org.bukkit.entity.Player) dev.meldrite.v1201.compat.NativeEntityExtensions.bukkit(viewer);
            getServer().getPluginManager().callEvent(visible ? new org.bukkit.event.player.PlayerShowEntityEvent(facade, this) : new org.bukkit.event.player.PlayerHideEntityEvent(facade, this));
        }
    }
    protected final NativeEntityPort entities;
    private volatile Entity nativeEntity;
    private final CraftPersistentDataContainer persistentDataContainer;
    public CraftEntity(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) {
        super(delegate, context);
        this.entities = java.util.Objects.requireNonNull(entities);
        this.nativeEntity = entities.require(delegate.identity().handle());
        this.persistentDataContainer = dev.meldrite.v1201.compat.NativeEntityExtensions.state(nativeEntity).meldrite$getPersistentDataContainer();
    }
    protected CraftEntity(CraftEntity projected) {
        this(projected.delegate, projected.context, projected.entities);
    }
    public Entity getHandle() {
        if (!delegate.valid()) throw new IllegalStateException("STALE_CRAFT_ENTITY");
        return nativeEntity;
    }
    @Override protected boolean meldrite$schedulerPresent() {
        Entity entity = nativeEntity;
        return !entity.isRemoved() && entity.getWorld() instanceof net.minecraft.server.world.ServerWorld world
                && world.getEntity(entity.getUuid()) == entity;
    }
    /** Replaces the native object while retaining this facade and its plugin state. */
    public void meldrite$rebindNative(Entity replacement) {
        java.util.Objects.requireNonNull(replacement, "replacement");
        if (!nativeEntity.getUuid().equals(replacement.getUuid())) throw new IllegalArgumentException("ENTITY_REBIND_IDENTITY_MISMATCH");
        var handle = entities.project(replacement);
        context.rebindEntity(delegate, handle, replacement.getId(), replacement.getName().getString(), this);
        nativeEntity = replacement;
    }
    public final boolean meldrite$isCurrentFor(FacadeContext expectedContext, Entity entity) {
        return context == expectedContext && nativeEntity == entity && delegate.valid();
    }
    public final boolean meldrite$isCachedFor(FacadeContext expectedContext, Entity entity) {
        return context == expectedContext && nativeEntity == entity;
    }
    @Override public CraftPersistentDataContainer getPersistentDataContainer() { return persistentDataContainer; }
    @Override public org.bukkit.event.entity.EntityDamageEvent getLastDamageCause() { return dev.meldrite.v1201.compat.NativeEntityExtensions.state(getHandle()).meldrite$getLastDamageCause(); }
    @Override public void setLastDamageCause(org.bukkit.event.entity.EntityDamageEvent event) { delegate.requireWritable(); dev.meldrite.v1201.compat.NativeEntityExtensions.state(getHandle()).meldrite$setLastDamageCause(event); }
    @Override public void setMetadata(String key, org.bukkit.metadata.MetadataValue value) {
        dev.meldrite.v1201.compat.NativeEntityExtensions.state(getHandle()).meldrite$setMetadata(
                java.util.Objects.requireNonNull(key, "key"), java.util.Objects.requireNonNull(value, "value"));
    }
    @Override public java.util.List<org.bukkit.metadata.MetadataValue> getMetadata(String key) {
        return dev.meldrite.v1201.compat.NativeEntityExtensions.state(getHandle()).meldrite$getMetadata(java.util.Objects.requireNonNull(key, "key"));
    }
    @Override public boolean hasMetadata(String key) {
        return dev.meldrite.v1201.compat.NativeEntityExtensions.state(getHandle()).meldrite$hasMetadata(java.util.Objects.requireNonNull(key, "key"));
    }
    @Override public void removeMetadata(String key, org.bukkit.plugin.Plugin plugin) {
        dev.meldrite.v1201.compat.NativeEntityExtensions.state(getHandle()).meldrite$removeMetadata(
                java.util.Objects.requireNonNull(key, "key"), java.util.Objects.requireNonNull(plugin, "plugin"));
    }
    public void storeBukkitValues(NbtCompound nbt) {
        if (!persistentDataContainer.isEmpty()) nbt.put("BukkitValues", persistentDataContainer.toTagCompound());
    }
    public void readBukkitValues(NbtCompound nbt) {
        if (nbt.contains("BukkitValues", NbtElement.COMPOUND_TYPE)) persistentDataContainer.putAll(nbt.getCompound("BukkitValues"));
    }
    @Override public boolean isValid() { return delegate.valid() && nativeEntity.isAlive() && dev.meldrite.v1201.compat.NativeEntityExtensions.state(nativeEntity).meldrite$isTracked(); }
    @Override public void playEffect(org.bukkit.EntityEffect effect) {
        if (effect == null) throw new IllegalArgumentException("Entity effect cannot be null");
        if (!effect.getApplicable().isInstance(this)) return;
        var entity = getHandle();
        dev.meldrite.v1201.compat.NativeEntityExtensions.requireThread(entity);
        entity.getWorld().sendEntityStatus(entity, effect.getData());
    }
    @Override public org.bukkit.Location getLocation() {
        Entity entity = getHandle();
        return new org.bukkit.Location(getWorld(), entity.getX(), entity.getY(), entity.getZ(), entity.getYaw(), entity.getPitch());
    }
    @Override public org.bukkit.Location getLocation(org.bukkit.Location location) {
        if (location == null) return null;
        Entity entity = getHandle();
        org.bukkit.World world = getWorld();
        double x = entity.getX(), y = entity.getY(), z = entity.getZ();
        float yaw = entity.getYaw(), pitch = entity.getPitch();
        location.setWorld(world);
        location.setX(x); location.setY(y); location.setZ(z);
        location.setYaw(yaw); location.setPitch(pitch);
        return location;
    }
    @Override public double getX() { return getHandle().getX(); }
    @Override public double getY() { return getHandle().getY(); }
    @Override public double getZ() { return getHandle().getZ(); }
    @Override public float getYaw() { return getHandle().getYaw(); }
    @Override public float getPitch() { return getHandle().getPitch(); }
    @Override public org.bukkit.entity.EntityType getType() {
        var id = net.minecraft.registry.Registries.ENTITY_TYPE.getId(getHandle().getType());
        var type = org.bukkit.Registry.ENTITY_TYPE.get(new org.bukkit.NamespacedKey(id.getNamespace(), id.getPath()));
        if (type == null) throw new IllegalStateException("ENTITY_TYPE_NOT_PROJECTED: " + id);
        return type;
    }
    @Override public org.bukkit.Server getServer() { return context.server(); }
    @Override public String getName() { return getHandle().getName().getString(); }
    @Override public boolean hasGravity() { return !getHandle().hasNoGravity(); }
    @Override public void setGravity(boolean gravity) { delegate.requireWritable(); getHandle().setNoGravity(!gravity); }
    @Override public boolean isInvulnerable() { return getHandle().isInvulnerable(); }
    @Override public void setInvulnerable(boolean invulnerable) { delegate.requireWritable(); getHandle().setInvulnerable(invulnerable); }
    @Override public boolean isSilent() { return getHandle().isSilent(); }
    @Override public void setSilent(boolean silent) { delegate.requireWritable(); getHandle().setSilent(silent); }
    @Override public boolean isGlowing() { return getHandle().isGlowing(); }
    @Override public void setGlowing(boolean glowing) { delegate.requireWritable(); getHandle().setGlowing(glowing); }
    @Override public boolean isOnGround() { return getHandle().isOnGround(); }
    @Override public int getTicksLived() { return getHandle().age; }
    @Override public void setTicksLived(int ticks) { if (ticks < 1) throw new IllegalArgumentException("ticks must be positive"); delegate.requireWritable(); getHandle().age = ticks; }
    @Override public int getFireTicks() { return getHandle().getFireTicks(); }
    @Override public void setFireTicks(int ticks) { delegate.requireWritable(); getHandle().setFireTicks(ticks); }
    @Override public int getFreezeTicks() { return getHandle().getFrozenTicks(); }
    @Override public void setFreezeTicks(int ticks) { if (ticks < 0) throw new IllegalArgumentException("ticks must be nonnegative"); delegate.requireWritable(); getHandle().setFrozenTicks(ticks); }
    @Override public int getMaxFreezeTicks() { return getHandle().getMinFreezeDamageTicks(); }
    @Override public boolean isFrozen() { return getHandle().isFrozen(); }
    @Override public boolean isInWater() { return getHandle().isTouchingWater(); }
    @Override public boolean isInLava() { return getHandle().isInLava(); }
    @Override public boolean isInWaterOrRain() { return getHandle().isTouchingWaterOrRain(); }
    @Override public boolean isInWaterOrBubbleColumn() { return getHandle().isInsideWaterOrBubbleColumn(); }
    @Override public boolean isInRain() {
        return ((dev.meldrite.v1201.hook.EntityPaperFlagAccessMixin) (Object) getHandle()).meldrite$isBeingRainedOn();
    }
    @Override public boolean isInBubbleColumn() {
        return ((dev.meldrite.v1201.hook.EntityPaperFlagAccessMixin) (Object) getHandle()).meldrite$isInsideBubbleColumn();
    }
    @Override public boolean isInWaterOrRainOrBubbleColumn() {
        return getHandle().isTouchingWaterOrRain() || isInBubbleColumn();
    }
    @Override public boolean isInPowderedSnow() {
        Entity entity = getHandle();
        return entity.inPowderSnow || entity.wasInPowderSnow;
    }
    @Override public boolean isUnderWater() { return getHandle().isSubmergedInWater(); }
    @Override public int getMaxFireTicks() { return getHandle().isFireImmune() ? 0 : 20; }
    @Override public boolean isVisualFire() { return dev.meldrite.v1201.compat.NativeEntityExtensions.state(getHandle()).meldrite$hasVisualFire(); }
    @Override public void setVisualFire(boolean visualFire) {
        delegate.requireWritable();
        dev.meldrite.v1201.compat.NativeEntityExtensions.state(getHandle()).meldrite$setVisualFire(visualFire);
    }
    @Override public org.bukkit.entity.Pose getPose() {
        return switch (getHandle().getPose()) {
            case CROUCHING -> org.bukkit.entity.Pose.SNEAKING;
            case STANDING -> org.bukkit.entity.Pose.STANDING;
            case FALL_FLYING -> org.bukkit.entity.Pose.FALL_FLYING;
            case SLEEPING -> org.bukkit.entity.Pose.SLEEPING;
            case SWIMMING -> org.bukkit.entity.Pose.SWIMMING;
            case SPIN_ATTACK -> org.bukkit.entity.Pose.SPIN_ATTACK;
            case LONG_JUMPING -> org.bukkit.entity.Pose.LONG_JUMPING;
            case DYING -> org.bukkit.entity.Pose.DYING;
            case CROAKING -> org.bukkit.entity.Pose.CROAKING;
            case USING_TONGUE -> org.bukkit.entity.Pose.USING_TONGUE;
            case SITTING -> org.bukkit.entity.Pose.SITTING;
            case ROARING -> org.bukkit.entity.Pose.ROARING;
            case SNIFFING -> org.bukkit.entity.Pose.SNIFFING;
            case EMERGING -> org.bukkit.entity.Pose.EMERGING;
            case DIGGING -> org.bukkit.entity.Pose.DIGGING;
        };
    }
    @Override public void setPose(org.bukkit.entity.Pose pose, boolean fixed) {
        java.util.Objects.requireNonNull(pose, "pose");
        delegate.requireWritable();
        var nativePose = switch (pose) {
            case SNEAKING -> net.minecraft.entity.EntityPose.CROUCHING;
            case STANDING -> net.minecraft.entity.EntityPose.STANDING;
            case FALL_FLYING -> net.minecraft.entity.EntityPose.FALL_FLYING;
            case SLEEPING -> net.minecraft.entity.EntityPose.SLEEPING;
            case SWIMMING -> net.minecraft.entity.EntityPose.SWIMMING;
            case SPIN_ATTACK -> net.minecraft.entity.EntityPose.SPIN_ATTACK;
            case LONG_JUMPING -> net.minecraft.entity.EntityPose.LONG_JUMPING;
            case DYING -> net.minecraft.entity.EntityPose.DYING;
            case CROAKING -> net.minecraft.entity.EntityPose.CROAKING;
            case USING_TONGUE -> net.minecraft.entity.EntityPose.USING_TONGUE;
            case SITTING -> net.minecraft.entity.EntityPose.SITTING;
            case ROARING -> net.minecraft.entity.EntityPose.ROARING;
            case SNIFFING -> net.minecraft.entity.EntityPose.SNIFFING;
            case EMERGING -> net.minecraft.entity.EntityPose.EMERGING;
            case DIGGING -> net.minecraft.entity.EntityPose.DIGGING;
        };
        getHandle().setPose(nativePose);
        dev.meldrite.v1201.compat.NativeEntityExtensions.state(getHandle()).meldrite$setFixedPose(fixed);
    }
    @Override public boolean hasFixedPose() { return dev.meldrite.v1201.compat.NativeEntityExtensions.state(getHandle()).meldrite$isFixedPose(); }
    @Override public boolean isSneaking() { return getHandle().isInSneakingPose(); }
    @Override public void setSneaking(boolean sneaking) {
        delegate.requireWritable();
        getHandle().setPose(sneaking ? net.minecraft.entity.EntityPose.CROUCHING : net.minecraft.entity.EntityPose.STANDING);
        dev.meldrite.v1201.compat.NativeEntityExtensions.state(getHandle()).meldrite$setFixedPose(false);
    }
    @Override public void setRotation(float yaw, float pitch) {
        if (!Float.isFinite(yaw) || !Float.isFinite(pitch)) throw new IllegalArgumentException("Rotation must be finite");
        delegate.requireWritable();
        ((dev.meldrite.v1201.hook.EntityPaperFlagAccessMixin) (Object) getHandle()).meldrite$setRotation(yaw, pitch);
    }
    @Override public org.bukkit.block.BlockFace getFacing() {
        return org.bukkit.craftbukkit.v1_20_R1.block.CraftBlock.notchToBlockFace(getHandle().getMovementDirection());
    }
    @Override public org.bukkit.entity.SpawnCategory getSpawnCategory() {
        return switch (getHandle().getType().getSpawnGroup()) {
            case MONSTER -> org.bukkit.entity.SpawnCategory.MONSTER;
            case CREATURE -> org.bukkit.entity.SpawnCategory.ANIMAL;
            case AMBIENT -> org.bukkit.entity.SpawnCategory.AMBIENT;
            case AXOLOTLS -> org.bukkit.entity.SpawnCategory.AXOLOTL;
            case UNDERGROUND_WATER_CREATURE -> org.bukkit.entity.SpawnCategory.WATER_UNDERGROUND_CREATURE;
            case WATER_CREATURE -> org.bukkit.entity.SpawnCategory.WATER_ANIMAL;
            case WATER_AMBIENT -> org.bukkit.entity.SpawnCategory.WATER_AMBIENT;
            case MISC -> org.bukkit.entity.SpawnCategory.MISC;
        };
    }
    @Override public org.bukkit.block.PistonMoveReaction getPistonMoveReaction() {
        return org.bukkit.block.PistonMoveReaction.getById(getHandle().getPistonBehavior().ordinal());
    }
    @Override public org.bukkit.Location getOrigin() { return getLocation(); }
    @Override public boolean collidesAt(org.bukkit.Location location) {
        java.util.Objects.requireNonNull(location, "location").checkFinite();
        if (location.getWorld() != getWorld()) return true;
        var entity = getHandle();
        var box = entity.getBoundingBox().offset(location.getX() - entity.getX(), location.getY() - entity.getY(), location.getZ() - entity.getZ());
        return !((net.minecraft.world.CollisionView) entity.getWorld()).isSpaceEmpty(entity, box);
    }
    @Override public boolean wouldCollideUsing(org.bukkit.util.BoundingBox box) {
        java.util.Objects.requireNonNull(box, "box");
        var nativeBox = new net.minecraft.util.math.Box(box.getMinX(), box.getMinY(), box.getMinZ(), box.getMaxX(), box.getMaxY(), box.getMaxZ());
        return !((net.minecraft.world.CollisionView) getHandle().getWorld()).isSpaceEmpty(getHandle(), nativeBox);
    }
    @Override public double getHeight() { return getHandle().getHeight(); }
    @Override public double getWidth() { return getHandle().getWidth(); }
    @Override public float getFallDistance() { return getHandle().fallDistance; }
    @Override public void setFallDistance(float distance) { if (!Float.isFinite(distance)) throw new IllegalArgumentException("Nonfinite fall distance"); delegate.requireWritable(); getHandle().fallDistance = distance; }
    @Override public org.bukkit.util.Vector getVelocity() { var velocity = getHandle().getVelocity(); return new org.bukkit.util.Vector(velocity.x, velocity.y, velocity.z); }
    @Override public void setVelocity(org.bukkit.util.Vector velocity) { java.util.Objects.requireNonNull(velocity).checkFinite(); delegate.requireWritable(); getHandle().setVelocity(velocity.getX(), velocity.getY(), velocity.getZ()); getHandle().velocityModified = true; }
    @Override public org.bukkit.util.BoundingBox getBoundingBox() { var box = getHandle().getBoundingBox(); return new org.bukkit.util.BoundingBox(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ); }
    @Override public java.util.List<org.bukkit.entity.Entity> getNearbyEntities(double x, double y, double z) {
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) throw new IllegalArgumentException("Nonfinite nearby bounds");
        var nativeEntity = getHandle(); dev.meldrite.v1201.compat.NativeEntityExtensions.requireThread(nativeEntity);
        var bounds = nativeEntity.getBoundingBox().expand(x, y, z);
        var result = new java.util.ArrayList<org.bukkit.entity.Entity>();
        for (var entity : nativeEntity.getWorld().getOtherEntities(nativeEntity, bounds, candidate -> true)) {
            result.add(dev.meldrite.v1201.compat.NativeEntityExtensions.bukkit(entity));
        }
        return result;
    }
    private static Component adventure(net.minecraft.text.Text text) {
        return GsonComponentSerializer.gson().deserialize(net.minecraft.text.Text.Serializer.toJson(text));
    }
    private static net.minecraft.text.Text nativeText(Component component) {
        return net.minecraft.text.Text.Serializer.fromJson(GsonComponentSerializer.gson().serialize(component));
    }
    @Override public Component customName() {
        var name = getHandle().getCustomName();
        return name == null ? null : adventure(name);
    }
    @Override public void customName(Component name) {
        delegate.requireWritable();
        getHandle().setCustomName(name == null ? null : nativeText(name));
    }
    @Override public Component name() { return adventure(getHandle().getName()); }
    @Override public boolean isCustomNameVisible() { return getHandle().isCustomNameVisible(); }
    @Override public void setCustomNameVisible(boolean visible) { delegate.requireWritable(); getHandle().setCustomNameVisible(visible); }
    @Override public String getCustomName() { var name = getHandle().getCustomName(); return name == null ? null : name.getString(); }
    @Override public void setCustomName(String name) { delegate.requireWritable(); getHandle().setCustomName(name == null ? null : net.minecraft.text.Text.literal(name)); }
    @Override public int getPortalCooldown() { return getHandle().getPortalCooldown(); }
    @Override public void setPortalCooldown(int cooldown) { delegate.requireWritable(); getHandle().setPortalCooldown(cooldown); }
    @Override public java.util.Set<String> getScoreboardTags() { return java.util.Set.copyOf(getHandle().getCommandTags()); }
    @Override public boolean addScoreboardTag(String tag) { delegate.requireWritable(); return getHandle().addCommandTag(java.util.Objects.requireNonNull(tag)); }
    @Override public boolean removeScoreboardTag(String tag) { delegate.requireWritable(); return getHandle().removeScoreboardTag(java.util.Objects.requireNonNull(tag)); }
    @Override public boolean isDead() { return !delegate.valid(); }
    @Override public boolean isPersistent() { return dev.meldrite.v1201.compat.NativeEntityExtensions.state(getHandle()).meldrite$isPersistent(); }
    @Override public void setPersistent(boolean persistent) {
        delegate.requireWritable();
        dev.meldrite.v1201.compat.NativeEntityExtensions.state(getHandle()).meldrite$setPersistent(persistent);
    }
    @Override public void remove() { context.remove(delegate); }
    @Override public org.bukkit.entity.Entity getVehicle() {
        Entity vehicle = getHandle().getVehicle();
        return vehicle == null ? null : dev.meldrite.v1201.compat.NativeEntityExtensions.bukkit(vehicle);
    }
    @Override public java.util.List<org.bukkit.entity.Entity> getPassengers() {
        return getHandle().getPassengerList().stream().map(entity -> (org.bukkit.entity.Entity)
                dev.meldrite.v1201.compat.NativeEntityExtensions.bukkit(entity)).toList();
    }
    @Override public org.bukkit.entity.Entity getPassenger() { return getPassengers().stream().findFirst().orElse(null); }
    @Override public boolean isInsideVehicle() { return getHandle().hasVehicle(); }
    @Override public boolean isEmpty() { return !getHandle().hasPassengers(); }
    @Override public boolean leaveVehicle() {
        delegate.requireWritable();
        Entity entity = getHandle();
        if (!entity.hasVehicle()) return false;
        dev.meldrite.v1201.compat.NativeEntityExtensions.dismount(entity, false);
        return !entity.hasVehicle();
    }
    @Override public boolean eject() {
        delegate.requireWritable();
        Entity entity = getHandle();
        if (!entity.hasPassengers()) return false;
        for (Entity passenger : java.util.List.copyOf(entity.getPassengerList())) {
            dev.meldrite.v1201.compat.NativeEntityExtensions.dismount(passenger, false);
        }
        return !entity.hasPassengers();
    }
    @Override public boolean addPassenger(org.bukkit.entity.Entity passenger) {
        delegate.requireWritable();
        if (!(passenger instanceof CraftEntity craft)) throw new IllegalArgumentException("FOREIGN_ENTITY_FACADE");
        return craft.getHandle().startRiding(getHandle(), true);
    }
    @Override public boolean setPassenger(org.bukkit.entity.Entity passenger) {
        java.util.Objects.requireNonNull(passenger, "passenger");
        eject();
        return addPassenger(passenger);
    }
    @Override public boolean removePassenger(org.bukkit.entity.Entity passenger) {
        delegate.requireWritable();
        if (!(passenger instanceof CraftEntity craft) || craft.getHandle().getVehicle() != getHandle()) return false;
        return craft.leaveVehicle();
    }
    @Override public boolean teleport(org.bukkit.Location location) {
        return teleport(location, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.PLUGIN);
    }
    @Override public boolean teleport(org.bukkit.Location location, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause cause) {
        return dev.meldrite.v1201.compat.NativeEntityTeleport.teleport(this, location, cause);
    }
    @Override public java.util.concurrent.CompletableFuture<Boolean> teleportAsync(org.bukkit.Location location, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause cause) {
        return dev.meldrite.v1201.compat.NativeEntityTeleport.teleportAsync(this, location, cause);
    }
    @Override public boolean teleport(org.bukkit.entity.Entity destination) {
        return teleport(destination, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.PLUGIN);
    }
    @Override public boolean teleport(org.bukkit.entity.Entity destination, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause cause) {
        java.util.Objects.requireNonNull(destination, "destination");
        return teleport(destination.getLocation(), cause);
    }
    @Override public <T extends org.bukkit.entity.Projectile> T launchProjectile(Class<? extends T> projectile) { throw new UnsupportedOperationException("ENTITY_PROJECTILE_SOURCE_REQUIRES_LIVING_ENTITY"); }
    @Override public <T extends org.bukkit.entity.Projectile> T launchProjectile(Class<? extends T> projectile, org.bukkit.util.Vector velocity) { throw new UnsupportedOperationException("ENTITY_PROJECTILE_SOURCE_REQUIRES_LIVING_ENTITY"); }
    @Override public <T extends org.bukkit.entity.Projectile> T launchProjectile(Class<? extends T> projectile, org.bukkit.util.Vector velocity, org.bukkit.util.Consumer<T> function) { throw new UnsupportedOperationException("ENTITY_PROJECTILE_SOURCE_REQUIRES_LIVING_ENTITY"); }
}
