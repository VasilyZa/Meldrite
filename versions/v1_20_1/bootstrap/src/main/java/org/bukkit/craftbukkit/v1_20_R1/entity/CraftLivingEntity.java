package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundEvent;

public class CraftLivingEntity extends CraftEntity implements org.bukkit.entity.LivingEntity {
    private final org.bukkit.craftbukkit.v1_20_R1.inventory.CraftEntityEquipment equipment = new org.bukkit.craftbukkit.v1_20_R1.inventory.CraftEntityEquipment(this);
    public CraftLivingEntity(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) {
        super(delegate, context, entities);
    }
    @Override public LivingEntity getHandle() { return (LivingEntity) super.getHandle(); }
    private dev.meldrite.v1201.hook.LivingEntityPaperAccessMixin state() { return (dev.meldrite.v1201.hook.LivingEntityPaperAccessMixin) getHandle(); }
    private static org.bukkit.Sound bukkitSound(SoundEvent sound) {
        if (sound == null) return null;
        var id = sound.getId();
        var projected = org.bukkit.Registry.SOUNDS.get(new org.bukkit.NamespacedKey(id.getNamespace(), id.getPath()));
        if (projected == null) throw new UnsupportedOperationException("UNREPRESENTABLE_NATIVE_SOUND: " + id);
        return projected;
    }
    private static net.minecraft.entity.ai.brain.MemoryModuleType<Object> memoryType(org.bukkit.entity.memory.MemoryKey<?> key) {
        java.util.Objects.requireNonNull(key, "memoryKey");
        var id = org.bukkit.craftbukkit.v1_20_R1.util.CraftNamespacedKey.toMinecraft(key.getKey());
        @SuppressWarnings("unchecked") var type = (net.minecraft.entity.ai.brain.MemoryModuleType<Object>) net.minecraft.registry.Registries.MEMORY_MODULE_TYPE.get(id);
        return type;
    }
    private Object bukkitMemory(Object value) {
        if (value == null) return null;
        if (value instanceof net.minecraft.util.math.GlobalPos position) {
            var nativeWorld = ((net.minecraft.server.world.ServerWorld) getHandle().getWorld()).getServer().getWorld(position.getDimension());
            if (nativeWorld == null) return null;
            var world = dev.meldrite.v1201.compat.NativeEntityExtensions.world(nativeWorld);
            var block = position.getPos();
            return new org.bukkit.Location(world, block.getX(), block.getY(), block.getZ());
        }
        if (value instanceof Long || value instanceof java.util.UUID || value instanceof Boolean || value instanceof Integer) return value;
        throw new UnsupportedOperationException("UNREPRESENTABLE_NATIVE_MEMORY: " + value.getClass().getName());
    }
    private static Object nativeMemory(Object value) {
        if (value == null || value instanceof Long || value instanceof java.util.UUID || value instanceof Boolean || value instanceof Integer) return value;
        if (value instanceof org.bukkit.Location location) {
            java.util.Objects.requireNonNull(location.getWorld(), "memoryLocation.world");
            if (!(location.getWorld() instanceof org.bukkit.craftbukkit.v1_20_R1.CraftWorld world)) throw new IllegalArgumentException("MEMORY_LOCATION_REQUIRES_CRAFT_WORLD");
            return net.minecraft.util.math.GlobalPos.create(world.getHandle().getRegistryKey(), new net.minecraft.util.math.BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
        }
        throw new UnsupportedOperationException("UNREPRESENTABLE_BUKKIT_MEMORY: " + value.getClass().getName());
    }
    public void requireWritable() { delegate.requireWritable(); }
    @Override public double getHealth() { return Math.min(Math.max(0, getHandle().getHealth()), getMaxHealth()); }
    @Override public void setHealth(double health) { if (!Double.isFinite(health) || health < 0 || health > getMaxHealth()) throw new IllegalArgumentException("Health outside entity limits"); requireWritable(); getHandle().setHealth((float) health); if (health == 0) getHandle().onDeath(getHandle().getDamageSources().generic()); }
    @Override public double getMaxHealth() { return getHandle().getMaxHealth(); }
    @Override public void setMaxHealth(double health) { if (!Double.isFinite(health) || health <= 0) throw new IllegalArgumentException("Max health must be finite and positive"); requireWritable(); getHandle().getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(health); if (getHandle().getHealth() > health) setHealth(health); }
    @Override public void resetMaxHealth() { setMaxHealth(net.minecraft.entity.attribute.EntityAttributes.GENERIC_MAX_HEALTH.getDefaultValue()); }
    @Override public double getAbsorptionAmount() { return getHandle().getAbsorptionAmount(); }
    @Override public void setAbsorptionAmount(double amount) { if (!Double.isFinite(amount) || amount < 0) throw new IllegalArgumentException("Absorption must be finite and nonnegative"); requireWritable(); getHandle().setAbsorptionAmount((float) amount); }
    @Override public void damage(double amount) { damage(amount, null); }
    @Override public void damage(double amount, org.bukkit.entity.Entity source) {
        if (!Double.isFinite(amount) || amount < 0) throw new IllegalArgumentException("Damage must be finite and nonnegative"); requireWritable();
        var damage = getHandle().getDamageSources().generic();
        if (source != null) { if (!(source instanceof CraftEntity craft)) throw new IllegalArgumentException("FOREIGN_ENTITY_FACADE"); var nativeSource = craft.getHandle(); if (nativeSource instanceof net.minecraft.entity.player.PlayerEntity player) damage = getHandle().getDamageSources().playerAttack(player); else if (nativeSource instanceof LivingEntity living) damage = getHandle().getDamageSources().mobAttack(living); }
        getHandle().damage(damage, (float) amount);
    }
    @Override public org.bukkit.attribute.AttributeInstance getAttribute(org.bukkit.attribute.Attribute attribute) {
        var key = java.util.Objects.requireNonNull(attribute).getKey(); var id = new net.minecraft.util.Identifier(key.getNamespace(), key.getKey());
        if (!net.minecraft.registry.Registries.ATTRIBUTE.containsId(id)) return null;
        var nativeAttribute = net.minecraft.registry.Registries.ATTRIBUTE.get(id); var instance = getHandle().getAttributeInstance(nativeAttribute);
        return instance == null ? null : new org.bukkit.craftbukkit.v1_20_R1.attribute.CraftAttributeInstance(this, attribute, instance);
    }
    @Override public double getEyeHeight() { return getHandle().getEyeHeight(getHandle().getPose()); }
    @Override public double getEyeHeight(boolean ignorePose) { return ignorePose ? getHandle().getStandingEyeHeight() : getEyeHeight(); }
    @Override public org.bukkit.Location getEyeLocation() { return getLocation().add(0, getEyeHeight(), 0); }
    @Override public int getRemainingAir() { return getHandle().getAir(); }
    @Override public void setRemainingAir(int air) { requireWritable(); getHandle().setAir(air); }
    @Override public int getMaximumAir() { return getHandle().getMaxAir(); }
    @Override public void setMaximumAir(int ticks) { requireWritable(); dev.meldrite.v1201.compat.NativeEntityExtensions.state(getHandle()).meldrite$setMaxAirOverride(ticks); }
    @Override public int getArrowCooldown() { return getHandle().stuckArrowTimer; }
    @Override public void setArrowCooldown(int ticks) { requireWritable(); getHandle().stuckArrowTimer = ticks; }
    @Override public int getArrowsInBody() { return getHandle().getStuckArrowCount(); }
    @Override public int getArrowsStuck() { return getArrowsInBody(); }
    @Override public void setArrowsInBody(int arrows, boolean fireEvent) {
        if (arrows < 0) throw new IllegalArgumentException("arrows must be nonnegative");
        requireWritable();
        if (!fireEvent) {
            getHandle().setStuckArrowCount(arrows);
            return;
        }
        var event = new org.bukkit.event.entity.ArrowBodyCountChangeEvent(this, getHandle().getStuckArrowCount(), arrows, false);
        org.bukkit.Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) getHandle().setStuckArrowCount(event.getNewAmount());
    }
    @Override public void setArrowsStuck(int arrows) { setArrowsInBody(arrows, false); }
    @Override public int getBeeStingerCooldown() { return getHandle().stuckStingerTimer; }
    @Override public void setBeeStingerCooldown(int ticks) { requireWritable(); getHandle().stuckStingerTimer = ticks; }
    @Override public int getBeeStingersInBody() { return getHandle().getStingerCount(); }
    @Override public void setBeeStingersInBody(int count) { if (count < 0) throw new IllegalArgumentException("stingers must be nonnegative"); requireWritable(); getHandle().setStingerCount(count); }
    @Override public double getLastDamage() { return state().meldrite$getLastDamage(); }
    @Override public void setLastDamage(double damage) { if (!Double.isFinite(damage)) throw new IllegalArgumentException("nonfinite damage"); requireWritable(); state().meldrite$setLastDamage((float) damage); }
    @Override public int getNoDamageTicks() { return getHandle().timeUntilRegen; }
    @Override public void setNoDamageTicks(int ticks) { getHandle().timeUntilRegen = ticks; }
    @Override public int getMaximumNoDamageTicks() { return dev.meldrite.v1201.compat.NativeEntityExtensions.state(getHandle()).meldrite$getMaximumNoDamageTicks(); }
    @Override public void setMaximumNoDamageTicks(int ticks) { requireWritable(); dev.meldrite.v1201.compat.NativeEntityExtensions.state(getHandle()).meldrite$setMaximumNoDamageTicks(ticks); }
    @Override public int getNoActionTicks() { return state().meldrite$getDespawnCounter(); }
    @Override public void setNoActionTicks(int ticks) { if (ticks < 0) throw new IllegalArgumentException("ticks must be nonnegative"); requireWritable(); state().meldrite$setDespawnCounter(ticks); }
    @Override public org.bukkit.entity.Player getKiller() { var player = state().meldrite$getAttackingPlayer(); return player == null ? null : (org.bukkit.entity.Player) dev.meldrite.v1201.compat.NativeEntityExtensions.bukkit(player); }
    @Override public void setKiller(org.bukkit.entity.Player player) { requireWritable(); if (player != null && !(player instanceof CraftPlayer)) throw new IllegalArgumentException("FOREIGN_ENTITY_FACADE"); state().meldrite$setAttackingPlayer(player == null ? null : ((CraftPlayer) player).getHandle()); }
    private static net.minecraft.entity.effect.StatusEffect effect(org.bukkit.potion.PotionEffectType effect) { var key = java.util.Objects.requireNonNull(effect).getKey(); var id = new net.minecraft.util.Identifier(key.getNamespace(), key.getKey()); if (!net.minecraft.registry.Registries.STATUS_EFFECT.containsId(id)) throw new IllegalArgumentException("Unknown effect: " + key); return net.minecraft.registry.Registries.STATUS_EFFECT.get(id); }
    private static org.bukkit.potion.PotionEffect effect(net.minecraft.entity.effect.StatusEffectInstance effect) { var id = net.minecraft.registry.Registries.STATUS_EFFECT.getId(effect.getEffectType()); var type = org.bukkit.potion.PotionEffectType.getByKey(new org.bukkit.NamespacedKey(id.getNamespace(), id.getPath())); if (type == null) throw new UnsupportedOperationException("UNREPRESENTABLE_NATIVE_EFFECT: " + id); return new org.bukkit.potion.PotionEffect(type, effect.getDuration(), effect.getAmplifier(), effect.isAmbient(), effect.shouldShowParticles(), effect.shouldShowIcon()); }
    @Override public boolean addPotionEffect(org.bukkit.potion.PotionEffect effect) { return addPotionEffect(effect, false); }
    @Override public boolean addPotionEffect(org.bukkit.potion.PotionEffect effect, boolean force) { requireWritable(); var nativeEffect = new net.minecraft.entity.effect.StatusEffectInstance(effect(effect.getType()), effect.getDuration(), effect.getAmplifier(), effect.isAmbient(), effect.hasParticles(), effect.hasIcon()); if (force) getHandle().removeStatusEffect(nativeEffect.getEffectType()); return getHandle().addStatusEffect(nativeEffect); }
    @Override public boolean addPotionEffects(java.util.Collection<org.bukkit.potion.PotionEffect> effects) { boolean result = true; for (var effect : effects) result &= addPotionEffect(effect); return result; }
    @Override public boolean hasPotionEffect(org.bukkit.potion.PotionEffectType effect) { return getHandle().hasStatusEffect(effect(effect)); }
    @Override public org.bukkit.potion.PotionEffect getPotionEffect(org.bukkit.potion.PotionEffectType effect) { var found = getHandle().getStatusEffect(effect(effect)); return found == null ? null : effect(found); }
    @Override public void removePotionEffect(org.bukkit.potion.PotionEffectType effect) { requireWritable(); getHandle().removeStatusEffect(effect(effect)); }
    @Override public java.util.Collection<org.bukkit.potion.PotionEffect> getActivePotionEffects() { return getHandle().getStatusEffects().stream().map(CraftLivingEntity::effect).toList(); }
    @Override public boolean clearActivePotionEffects() { requireWritable(); return getHandle().clearStatusEffects(); }
    @Override public org.bukkit.inventory.EntityEquipment getEquipment() { return equipment; }
    @Override public boolean getRemoveWhenFarAway() { return getHandle() instanceof net.minecraft.entity.mob.MobEntity mob && !mob.isPersistent(); }
    @Override public void setRemoveWhenFarAway(boolean remove) { requireWritable(); if (getHandle() instanceof net.minecraft.entity.mob.MobEntity mob) ((dev.meldrite.v1201.hook.MobEntityPaperAccessMixin) mob).meldrite$setPersistenceRequired(!remove); }
    @Override public boolean getCanPickupItems() { return dev.meldrite.v1201.compat.NativeLivingPickup.get(getHandle()); }
    @Override public void setCanPickupItems(boolean pickup) { requireWritable(); dev.meldrite.v1201.compat.NativeLivingPickup.set(getHandle(), pickup); }
    @Override public boolean isGliding() { return getHandle().isFallFlying(); }
    @Override public void setGliding(boolean gliding) { requireWritable(); ((dev.meldrite.v1201.hook.EntityPaperFlagAccessMixin) getHandle()).meldrite$setFlag(7, gliding); }
    @Override public boolean isDead() { return !delegate.valid() || !getHandle().isAlive(); }
    @Override public boolean isSwimming() { return getHandle().isSwimming(); }
    @Override public void setSwimming(boolean swimming) { requireWritable(); getHandle().setSwimming(swimming); }
    @Override public boolean isRiptiding() { return getHandle().isUsingRiptide(); }
    @Override public boolean isSleeping() { return getHandle().isSleeping(); }
    @Override public boolean isClimbing() { return getHandle().isClimbing(); }
    @Override public void setAI(boolean ai) { requireWritable(); if (getHandle() instanceof net.minecraft.entity.mob.MobEntity mob) mob.setAiDisabled(!ai); }
    @Override public boolean hasAI() { return !(getHandle() instanceof net.minecraft.entity.mob.MobEntity mob) || !mob.isAiDisabled(); }
    @Override public void attack(org.bukkit.entity.Entity target) { requireWritable(); if (!(target instanceof CraftEntity craft)) throw new IllegalArgumentException("FOREIGN_ENTITY_FACADE"); if (getHandle() instanceof net.minecraft.entity.player.PlayerEntity player) player.attack(craft.getHandle()); else getHandle().tryAttack(craft.getHandle()); }
    @Override public void swingMainHand() { requireWritable(); getHandle().swingHand(net.minecraft.util.Hand.MAIN_HAND); }
    @Override public void swingOffHand() { requireWritable(); getHandle().swingHand(net.minecraft.util.Hand.OFF_HAND); }
    @Override public void setInvisible(boolean invisible) { requireWritable(); getHandle().setInvisible(invisible); }
    @Override public boolean isInvisible() { return getHandle().isInvisible(); }
    @Override public boolean canBreatheUnderwater() { return getHandle().canBreatheInWater(); }
    @Override public org.bukkit.inventory.ItemStack getActiveItem() { return org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack.asCraftMirror(getHandle().getActiveItem()); }
    @Override public void clearActiveItem() { requireWritable(); getHandle().clearActiveItem(); }
    @Override public int getItemUseRemainingTime() { return getHandle().getItemUseTimeLeft(); }
    @Override public int getHandRaisedTime() { return getHandle().getItemUseTime(); }
    @Override public boolean isHandRaised() { return getHandle().isUsingItem(); }
    @Override public org.bukkit.inventory.EquipmentSlot getHandRaised() { return getHandle().getActiveHand() == net.minecraft.util.Hand.MAIN_HAND ? org.bukkit.inventory.EquipmentSlot.HAND : org.bukkit.inventory.EquipmentSlot.OFF_HAND; }
    @Override public boolean isJumping() { return state().meldrite$isJumping(); }
    @Override public void setJumping(boolean jumping) { requireWritable(); getHandle().setJumping(jumping); if (jumping && getHandle() instanceof net.minecraft.entity.mob.MobEntity mob) mob.getJumpControl().setActive(); }
    @Override public float getHurtDirection() { return getHandle().getDamageTiltYaw(); }
    @Override public void setHurtDirection(float direction) { throw new UnsupportedOperationException("Cannot set the hurt direction on a non player"); }
    @Override public void knockback(double strength, double x, double z) { if (!Double.isFinite(strength) || !Double.isFinite(x) || !Double.isFinite(z)) throw new IllegalArgumentException("nonfinite knockback"); requireWritable(); getHandle().takeKnockback(strength, x, z); }
    @Override public float getBodyYaw() { return getHandle().bodyYaw; }
    @Override public void setBodyYaw(float yaw) { if (!Float.isFinite(yaw)) throw new IllegalArgumentException("nonfinite yaw"); requireWritable(); getHandle().setBodyYaw(yaw); }
    @Override public boolean hasLineOfSight(org.bukkit.entity.Entity target) { if (!(target instanceof CraftEntity craft)) throw new IllegalArgumentException("FOREIGN_ENTITY_FACADE"); return getHandle().canSee(craft.getHandle()); }
    private java.util.List<org.bukkit.block.Block> lineOfSight(java.util.Set<org.bukkit.Material> transparent, int distance, int length) {
        dev.meldrite.v1201.compat.NativeEntityExtensions.requireThread(getHandle());
        if (transparent == null) transparent = java.util.Set.of(org.bukkit.Material.AIR, org.bukkit.Material.CAVE_AIR, org.bukkit.Material.VOID_AIR);
        var blocks = new java.util.ArrayList<org.bukkit.block.Block>();
        var iterator = new org.bukkit.util.BlockIterator(this, Math.min(distance, 120));
        while (iterator.hasNext()) {
            var block = iterator.next(); blocks.add(block);
            if (length != 0 && blocks.size() > length) blocks.remove(0);
            if (!transparent.contains(block.getType())) break;
        }
        return blocks;
    }
    @Override public java.util.List<org.bukkit.block.Block> getLineOfSight(java.util.Set<org.bukkit.Material> transparent, int distance) { return lineOfSight(transparent, distance, 0); }
    @Override public java.util.List<org.bukkit.block.Block> getLastTwoTargetBlocks(java.util.Set<org.bukkit.Material> transparent, int distance) { return lineOfSight(transparent, distance, 2); }
    @Override public org.bukkit.block.Block getTargetBlock(java.util.Set<org.bukkit.Material> transparent, int distance) { return lineOfSight(transparent, distance, 1).get(0); }
    @Override public org.bukkit.block.Block getTargetBlock(int distance, com.destroystokyo.paper.block.TargetBlockInfo.FluidMode fluid) { return getTargetBlockExact(distance, java.util.Objects.requireNonNull(fluid).bukkit); }
    @Override public org.bukkit.block.Block getTargetBlockExact(int distance) { return getTargetBlockExact(distance, org.bukkit.FluidCollisionMode.NEVER); }
    @Override public org.bukkit.block.Block getTargetBlockExact(int distance, org.bukkit.FluidCollisionMode fluid) { var hit = rayTraceBlocks(distance, fluid); return hit == null ? null : hit.getHitBlock(); }
    @Override public org.bukkit.block.BlockFace getTargetBlockFace(int distance, com.destroystokyo.paper.block.TargetBlockInfo.FluidMode fluid) { return getTargetBlockFace(distance, java.util.Objects.requireNonNull(fluid).bukkit); }
    @Override public org.bukkit.block.BlockFace getTargetBlockFace(int distance, org.bukkit.FluidCollisionMode fluid) { var hit = rayTraceBlocks(distance, fluid); return hit == null ? null : hit.getHitBlockFace(); }
    @Override public com.destroystokyo.paper.block.TargetBlockInfo getTargetBlockInfo(int distance, com.destroystokyo.paper.block.TargetBlockInfo.FluidMode fluid) { var hit = rayTraceBlocks(distance, java.util.Objects.requireNonNull(fluid).bukkit); return hit == null ? null : new com.destroystokyo.paper.block.TargetBlockInfo(hit.getHitBlock(), hit.getHitBlockFace()); }
    private static net.minecraft.world.RaycastContext.FluidHandling nativeFluid(org.bukkit.FluidCollisionMode fluid) {
        return switch (java.util.Objects.requireNonNull(fluid)) {
            case NEVER -> net.minecraft.world.RaycastContext.FluidHandling.NONE;
            case SOURCE_ONLY -> net.minecraft.world.RaycastContext.FluidHandling.SOURCE_ONLY;
            case ALWAYS -> net.minecraft.world.RaycastContext.FluidHandling.ANY;
        };
    }
    private net.minecraft.world.RaycastContext blockRayContext(net.minecraft.util.math.Vec3d start, net.minecraft.util.math.Vec3d end,
            net.minecraft.world.RaycastContext.ShapeType shape, net.minecraft.world.RaycastContext.FluidHandling fluid,
            net.minecraft.block.ShapeContext collision) {
        // Yarn requires a real entity in its constructor; Leaf world rays use an absent shape context.
        return new net.minecraft.world.RaycastContext(start, end, shape, fluid, getHandle()) {
            @Override public net.minecraft.util.shape.VoxelShape getBlockShape(net.minecraft.block.BlockState state,
                    net.minecraft.world.BlockView world, net.minecraft.util.math.BlockPos position) {
                return shape.get(state, world, position, collision);
            }
        };
    }
    @Override public org.bukkit.util.RayTraceResult rayTraceBlocks(double distance) { return rayTraceBlocks(distance, org.bukkit.FluidCollisionMode.NEVER); }
    @Override public org.bukkit.util.RayTraceResult rayTraceBlocks(double distance, org.bukkit.FluidCollisionMode fluid) {
        var handling = nativeFluid(fluid);
        if (!Double.isFinite(distance)) throw new IllegalArgumentException("Nonfinite ray distance");
        if (distance < 0) return null;
        var entity = getHandle(); dev.meldrite.v1201.compat.NativeEntityExtensions.requireThread(entity);
        var start = entity.getEyePos(); var end = start.add(entity.getRotationVector().multiply(distance));
        var hit = entity.getWorld().raycast(blockRayContext(start, end,
                net.minecraft.world.RaycastContext.ShapeType.OUTLINE, handling, net.minecraft.block.ShapeContext.absent()));
        if (hit.getType() == net.minecraft.util.hit.HitResult.Type.MISS) return null;
        var position = hit.getBlockPos(); var point = hit.getPos();
        return new org.bukkit.util.RayTraceResult(new org.bukkit.util.Vector(point.x, point.y, point.z),
                getWorld().getBlockAt(position.getX(), position.getY(), position.getZ()), org.bukkit.block.BlockFace.valueOf(hit.getSide().name()));
    }
    @Override public boolean hasLineOfSight(org.bukkit.Location location) {
        java.util.Objects.requireNonNull(location).checkFinite();
        if (location.getWorld() != getWorld()) return false;
        var entity = getHandle(); dev.meldrite.v1201.compat.NativeEntityExtensions.requireThread(entity);
        var start = entity.getEyePos(); var end = new net.minecraft.util.math.Vec3d(location.getX(), location.getY(), location.getZ());
        if (start.squaredDistanceTo(end) > 128.0 * 128.0) return false;
        return entity.getWorld().raycast(blockRayContext(start, end,
                net.minecraft.world.RaycastContext.ShapeType.COLLIDER, net.minecraft.world.RaycastContext.FluidHandling.NONE,
                net.minecraft.block.ShapeContext.of(entity))).getType() == net.minecraft.util.hit.HitResult.Type.MISS;
    }
    @Override public void broadcastSlotBreak(org.bukkit.inventory.EquipmentSlot slot) {
        requireWritable();
        getHandle().sendEquipmentBreakStatus(org.bukkit.craftbukkit.v1_20_R1.inventory.CraftEntityEquipment.nativeSlot(slot));
    }
    @Override public void broadcastSlotBreak(org.bukkit.inventory.EquipmentSlot slot, java.util.Collection<org.bukkit.entity.Player> players) {
        java.util.Objects.requireNonNull(slot, "slot");
        java.util.Objects.requireNonNull(players, "players");
        if (players.isEmpty()) return;
        byte status = switch (java.util.Objects.requireNonNull(slot)) {
            case HAND -> 47;
            case OFF_HAND -> 48;
            case HEAD -> 49;
            case CHEST -> 50;
            case LEGS -> 51;
            case FEET -> 52;
        };
        for (var player : players) {
            if (!(player instanceof CraftPlayer craftPlayer)) throw new IllegalArgumentException("FOREIGN_PLAYER_FACADE");
            var serverPlayer = craftPlayer.getHandle();
            serverPlayer.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket(getHandle(), status));
        }
    }
    @Override public void damageItemStack(org.bukkit.inventory.EquipmentSlot slot, int amount) {
        if (amount < 0) throw new IllegalArgumentException("amount must be nonnegative");
        requireWritable();
        var nativeStack = getHandle().getEquippedStack(org.bukkit.craftbukkit.v1_20_R1.inventory.CraftEntityEquipment.nativeSlot(slot));
        if (nativeStack.isEmpty()) return;
        nativeStack.damage(amount, getHandle(), entity -> broadcastSlotBreak(slot));
    }
    @Override public org.bukkit.inventory.ItemStack damageItemStack(org.bukkit.inventory.ItemStack stack, int amount) {
        java.util.Objects.requireNonNull(stack, "stack");
        if (amount < 0) throw new IllegalArgumentException("amount must be nonnegative");
        var nativeStack = org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack.asNMSCopy(stack);
        if (nativeStack.isEmpty()) return stack;
        nativeStack.damage(amount, getHandle(), entity -> { });
        return org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack.asCraftMirror(nativeStack);
    }
    @Override public org.bukkit.entity.EntityCategory getCategory() {
        var group = getHandle().getGroup();
        if (group == net.minecraft.entity.EntityGroup.UNDEAD) return org.bukkit.entity.EntityCategory.UNDEAD;
        if (group == net.minecraft.entity.EntityGroup.ARTHROPOD) return org.bukkit.entity.EntityCategory.ARTHROPOD;
        if (group == net.minecraft.entity.EntityGroup.ILLAGER) return org.bukkit.entity.EntityCategory.ILLAGER;
        if (group == net.minecraft.entity.EntityGroup.AQUATIC) return org.bukkit.entity.EntityCategory.WATER;
        if (group == net.minecraft.entity.EntityGroup.DEFAULT) return org.bukkit.entity.EntityCategory.NONE;
        throw new UnsupportedOperationException("UNREPRESENTABLE_NATIVE_ENTITY_GROUP: " + group);
    }
    @Override public java.util.Set<java.util.UUID> getCollidableExemptions() { return dev.meldrite.v1201.compat.NativeEntityExtensions.state(getHandle()).meldrite$getCollidableExemptions(); }
    @Override public org.bukkit.Sound getDeathSound() { return bukkitSound(state().meldrite$getDeathSound()); }
    @Override public org.bukkit.Sound getDrinkingSound(org.bukkit.inventory.ItemStack stack) { return bukkitSound(state().meldrite$getDrinkSound(org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack.asNMSCopy(java.util.Objects.requireNonNull(stack, "itemStack")))); }
    @Override public org.bukkit.Sound getEatingSound(org.bukkit.inventory.ItemStack stack) { return bukkitSound(state().meldrite$getEatSound(org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack.asNMSCopy(java.util.Objects.requireNonNull(stack, "itemStack")))); }
    @Override public org.bukkit.Sound getFallDamageSound(int fallHeight) { return bukkitSound(state().meldrite$getFallSound(fallHeight)); }
    @Override public org.bukkit.Sound getFallDamageSoundBig() { return bukkitSound(getHandle().getFallSounds().big()); }
    @Override public org.bukkit.Sound getFallDamageSoundSmall() { return bukkitSound(getHandle().getFallSounds().small()); }
    @Override public net.kyori.adventure.util.TriState getFrictionState() { return dev.meldrite.v1201.compat.NativeEntityExtensions.state(getHandle()).meldrite$getFrictionState(); }
    @Override public org.bukkit.Sound getHurtSound() { return bukkitSound(state().meldrite$getHurtSound(getHandle().getDamageSources().generic())); }
    @Override public org.bukkit.entity.Entity getLeashHolder() throws java.lang.IllegalStateException {
        if (!(getHandle() instanceof net.minecraft.entity.mob.MobEntity mob) || !mob.isLeashed()) throw new IllegalStateException("ENTITY_NOT_LEASHED");
        var holder = mob.getHoldingEntity();
        if (holder == null) throw new IllegalStateException("ENTITY_LEASH_HOLDER_MISSING");
        return dev.meldrite.v1201.compat.NativeEntityExtensions.bukkit(holder);
    }
    @Override @SuppressWarnings("unchecked") public <T extends java.lang.Object> T getMemory(org.bukkit.entity.memory.MemoryKey<T> p0) {
        var memory = getHandle().getBrain().getOptionalMemory(memoryType(p0));
        return memory == null ? null : (T) memory.map(this::bukkitMemory).orElse(null);
    }
    @Override public int getShieldBlockingDelay() { return dev.meldrite.v1201.compat.NativeEntityExtensions.state(getHandle()).meldrite$getShieldBlockingDelay(); }
    private org.bukkit.util.RayTraceResult rayTraceTargetEntity(int distance, boolean ignoreBlocks) {
        if (distance < 0) return null;
        var eye = getEyeLocation();
        var result = getWorld().rayTraceEntities(eye, eye.getDirection(), distance, 0, entity -> entity != this);
        if (result == null || ignoreBlocks) return result;
        var block = rayTraceBlocks(distance);
        if (block != null && block.getHitPosition().distanceSquared(eye.toVector()) <= result.getHitPosition().distanceSquared(eye.toVector())) return null;
        return result;
    }
    @Override public org.bukkit.entity.Entity getTargetEntity(int distance, boolean ignoreBlocks) {
        var hit = rayTraceTargetEntity(distance, ignoreBlocks);
        return hit == null ? null : hit.getHitEntity();
    }
    @Override public com.destroystokyo.paper.entity.TargetEntityInfo getTargetEntityInfo(int distance, boolean ignoreBlocks) {
        var hit = rayTraceTargetEntity(distance, ignoreBlocks);
        return hit == null ? null : new com.destroystokyo.paper.entity.TargetEntityInfo(hit.getHitEntity(), hit.getHitPosition());
    }
    @Override public boolean isCollidable() { return dev.meldrite.v1201.compat.NativeEntityExtensions.state(getHandle()).meldrite$isCollidable(); }
    @Override public boolean isLeashed() { return getHandle() instanceof net.minecraft.entity.mob.MobEntity mob && mob.isLeashed(); }
    @Override public <T extends org.bukkit.entity.Projectile> T launchProjectile(java.lang.Class<? extends T> projectile) { return launchProjectile(projectile, null, null); }
    @Override public <T extends org.bukkit.entity.Projectile> T launchProjectile(java.lang.Class<? extends T> projectile, org.bukkit.util.Vector velocity) { return launchProjectile(projectile, velocity, null); }
    @Override @SuppressWarnings("unchecked") public <T extends org.bukkit.entity.Projectile> T launchProjectile(java.lang.Class<? extends T> projectile, org.bukkit.util.Vector velocity, org.bukkit.util.Consumer<T> function) {
        java.util.Objects.requireNonNull(projectile, "projectile");
        if (getHandle().getWorld().isClient()) throw new IllegalStateException("Cannot launch projectile in client world");
        requireWritable();
        var world = getHandle().getWorld();
        var direction = getHandle().getRotationVector();
        net.minecraft.entity.Entity launch = null;
        if (projectile == org.bukkit.entity.Snowball.class) launch = new net.minecraft.entity.projectile.thrown.SnowballEntity(world, getHandle());
        else if (projectile == org.bukkit.entity.Egg.class) launch = new net.minecraft.entity.projectile.thrown.EggEntity(world, getHandle());
        else if (projectile == org.bukkit.entity.EnderPearl.class) launch = new net.minecraft.entity.projectile.thrown.EnderPearlEntity(world, getHandle());
        else if (projectile == org.bukkit.entity.Arrow.class) launch = new net.minecraft.entity.projectile.ArrowEntity(world, getHandle());
        else if (projectile == org.bukkit.entity.SpectralArrow.class) launch = new net.minecraft.entity.projectile.SpectralArrowEntity(world, getHandle());
        else if (projectile == org.bukkit.entity.Trident.class) launch = new net.minecraft.entity.projectile.TridentEntity(world, getHandle(), new net.minecraft.item.ItemStack(net.minecraft.item.Items.TRIDENT));
        else if (projectile == org.bukkit.entity.ThrownPotion.class) {
            var potion = new net.minecraft.entity.projectile.thrown.PotionEntity(world, getHandle());
            potion.setItem(org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(org.bukkit.Material.SPLASH_POTION)));
            launch = potion;
        } else if (projectile == org.bukkit.entity.ThrownExpBottle.class) launch = new net.minecraft.entity.projectile.thrown.ExperienceBottleEntity(world, getHandle());
        else if (projectile == org.bukkit.entity.SmallFireball.class) launch = new net.minecraft.entity.projectile.SmallFireballEntity(world, getHandle(), direction.x * 10, direction.y * 10, direction.z * 10);
        else if (projectile == org.bukkit.entity.WitherSkull.class) launch = new net.minecraft.entity.projectile.WitherSkullEntity(world, getHandle(), direction.x * 10, direction.y * 10, direction.z * 10);
        else if (projectile == org.bukkit.entity.DragonFireball.class) launch = new net.minecraft.entity.projectile.DragonFireballEntity(world, getHandle(), direction.x * 10, direction.y * 10, direction.z * 10);
        else if (projectile == org.bukkit.entity.Fireball.class) launch = new net.minecraft.entity.projectile.FireballEntity(world, getHandle(), direction.x * 10, direction.y * 10, direction.z * 10, 1);
        else if (projectile == org.bukkit.entity.Firework.class) launch = new net.minecraft.entity.projectile.FireworkRocketEntity(world, org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(org.bukkit.Material.FIREWORK_ROCKET)), getHandle());
        else throw new UnsupportedOperationException("UNSUPPORTED_NATIVE_PROJECTILE: " + projectile.getName());
        var eye = getHandle().getEyePos();
        launch.refreshPositionAndAngles(eye.x, eye.y, eye.z, getHandle().getYaw(), getHandle().getPitch());
        if (velocity != null) { velocity.checkFinite(); launch.setVelocity(velocity.getX(), velocity.getY(), velocity.getZ()); }
        if (function != null) function.accept((T) dev.meldrite.v1201.compat.NativeEntityExtensions.bukkit(launch));
        if (!world.spawnEntity(launch)) throw new IllegalStateException("PROJECTILE_SPAWN_FAILED");
        return (T) dev.meldrite.v1201.compat.NativeEntityExtensions.bukkit(launch);
    }
    @Override public void playHurtAnimation(float yaw) {
        if (!Float.isFinite(yaw)) throw new IllegalArgumentException("yaw must be finite");
        requireWritable();
        var world = getHandle().getWorld();
        if (!(world instanceof net.minecraft.server.world.ServerWorld serverWorld)) return;
        serverWorld.getChunkManager().sendToOtherNearbyPlayers(getHandle(),
                new net.minecraft.network.packet.s2c.play.DamageTiltS2CPacket(getHandle().getId(), yaw + 90.0F));
    }
    @Override public void playPickupItemAnimation(org.bukkit.entity.Item item, int quantity) {
        java.util.Objects.requireNonNull(item, "item");
        if (quantity < 0) throw new IllegalArgumentException("quantity must be nonnegative");
        requireWritable();
        if (!(item instanceof org.bukkit.craftbukkit.v1_20_R1.entity.CraftItem craft)) {
            throw new IllegalArgumentException("item must be a live CraftItem entity");
        }
        var nativeItem = craft.getHandle();
        if (nativeItem.isRemoved() || getHandle().getWorld().isClient()) return;
        ((net.minecraft.server.world.ServerWorld) getHandle().getWorld()).getChunkManager().sendToNearbyPlayers(
                nativeItem, new net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket(
                        nativeItem.getId(), getHandle().getId(), quantity));
    }
    @Override public org.bukkit.util.RayTraceResult rayTraceEntities(int distance, boolean ignoreBlocks) { return rayTraceTargetEntity(distance, ignoreBlocks); }
    @Override public void registerAttribute(org.bukkit.attribute.Attribute attribute) {
        java.util.Objects.requireNonNull(attribute, "attribute");
        requireWritable();
        var key = attribute.getKey();
        var id = new net.minecraft.util.Identifier(key.getNamespace(), key.getKey());
        var nativeAttribute = net.minecraft.registry.Registries.ATTRIBUTE.get(id);
        if (nativeAttribute == null) throw new IllegalArgumentException("Unknown attribute: " + key);
        ((dev.meldrite.v1201.compat.NativeAttributeRegistration) getHandle().getAttributes()).registerAttribute(nativeAttribute);
    }
    @Override public void setCollidable(boolean p0) { requireWritable(); dev.meldrite.v1201.compat.NativeEntityExtensions.state(getHandle()).meldrite$setCollidable(p0); }
    @Override public void setFrictionState(net.kyori.adventure.util.TriState state) { requireWritable(); dev.meldrite.v1201.compat.NativeEntityExtensions.state(getHandle()).meldrite$setFrictionState(state); }
    @Override public boolean setLeashHolder(org.bukkit.entity.Entity holder) {
        requireWritable();
        if (!(getHandle() instanceof net.minecraft.entity.mob.MobEntity mob) || getHandle() instanceof net.minecraft.entity.boss.WitherEntity) return false;
        if (holder == null) {
            if (!mob.isLeashed()) return false;
            mob.detachLeash(true, false);
            return true;
        }
        if (!(holder instanceof CraftEntity craft) || !craft.getHandle().isAlive()) return false;
        mob.detachLeash(true, false);
        mob.attachLeash(craft.getHandle(), true);
        return true;
    }
    @Override public <T extends java.lang.Object> void setMemory(org.bukkit.entity.memory.MemoryKey<T> p0, T p1) {
        requireWritable();
        var type = memoryType(p0);
        if (p1 == null) getHandle().getBrain().forget(type);
        else getHandle().getBrain().remember(type, nativeMemory(p1));
    }
    @Override public void setShieldBlockingDelay(int delay) { if (delay < 0) throw new IllegalArgumentException("delay must be nonnegative"); requireWritable(); dev.meldrite.v1201.compat.NativeEntityExtensions.state(getHandle()).meldrite$setShieldBlockingDelay(delay); }
}
