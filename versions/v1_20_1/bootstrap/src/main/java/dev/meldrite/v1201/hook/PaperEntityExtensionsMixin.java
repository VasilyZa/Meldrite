package dev.meldrite.v1201.hook;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.meldrite.v1201.compat.NativeEntityExtensions;
import dev.meldrite.v1201.compat.PaperEntityAccess;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R1.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.v1_20_R1.persistence.CraftPersistentDataTypeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.IdentityHashMap;
import net.kyori.adventure.util.TriState;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

@Mixin(Entity.class)
public abstract class PaperEntityExtensionsMixin implements PaperEntityAccess {
    @Shadow private Entity vehicle;
    @Unique private boolean meldrite$visibleByDefault = true;
    @Override public boolean meldrite$isVisibleByDefault() { return meldrite$visibleByDefault; }
    @Override public void meldrite$setVisibleByDefault(boolean visible) { meldrite$visibleByDefault = visible; }
    public boolean persist = true;
    public boolean valid;
    public boolean lastDamageCancelled;
    @Unique private org.bukkit.event.entity.EntityDamageEvent meldrite$lastDamageCause;
    @Unique private static final CraftPersistentDataTypeRegistry MELDRITE_DATA_TYPES = new CraftPersistentDataTypeRegistry();
    @Unique private CraftPersistentDataContainer meldrite$persistentDataContainer;
    @Unique private volatile CraftEntity meldrite$bukkitEntity;
    @Unique private int meldrite$maximumNoDamageTicks = 20;
    @Unique private boolean meldrite$maxAirOverridden;
    @Unique private int meldrite$maxAirOverride;
    @Unique private boolean meldrite$collidable = true;
    @Unique private boolean meldrite$collidableOverridden;
    @Unique private Set<UUID> meldrite$collidableExemptions;
    @Unique private TriState meldrite$frictionState = TriState.NOT_SET;
    @Unique private int meldrite$shieldBlockingDelay = 5;
    @Unique private boolean meldrite$fixedPose;
    @Unique private boolean meldrite$visualFire;
    @Unique private Map<String, IdentityHashMap<Plugin, MetadataValue>> meldrite$metadata;

    @Override public synchronized SessionState meldrite$exportSessionState() {
        return new SessionState(meldrite$visibleByDefault, persist, meldrite$getPersistentDataContainer(),
                meldrite$maximumNoDamageTicks, meldrite$lastDamageCause, lastDamageCancelled, meldrite$maxAirOverridden,
                meldrite$maxAirOverride, meldrite$collidable, meldrite$collidableOverridden, meldrite$collidableExemptions,
                meldrite$frictionState, meldrite$shieldBlockingDelay, meldrite$fixedPose, meldrite$visualFire, meldrite$metadata);
    }
    @Override public synchronized void meldrite$importSessionState(SessionState state) {
        java.util.Objects.requireNonNull(state, "state");
        meldrite$visibleByDefault = state.visibleByDefault(); persist = state.persistent();
        meldrite$persistentDataContainer = state.persistentData(); meldrite$maximumNoDamageTicks = state.maximumNoDamageTicks();
        meldrite$lastDamageCause = state.lastDamageCause(); lastDamageCancelled = state.lastDamageCancelled();
        meldrite$maxAirOverridden = state.maxAirOverridden(); meldrite$maxAirOverride = state.maxAir();
        meldrite$collidable = state.collidable(); meldrite$collidableOverridden = state.collidableOverridden();
        meldrite$collidableExemptions = state.collidableExemptions(); meldrite$frictionState = state.friction();
        meldrite$shieldBlockingDelay = state.shieldBlockingDelay(); meldrite$fixedPose = state.fixedPose();
        meldrite$visualFire = state.visualFire(); meldrite$metadata = state.metadata();
    }

    public CraftEntity getBukkitEntity() { return NativeEntityExtensions.bukkit((Entity) (Object) this); }
    public CraftEntity getBukkitEntityRaw() { return meldrite$bukkitEntity; }
    public void stopRiding(boolean suppressCancellation) {
        NativeEntityExtensions.dismount((Entity) (Object) this, suppressCancellation);
    }
    @Override public boolean meldrite$isPersistent() { return persist; }
    @Override public void meldrite$setPersistent(boolean value) { persist = value; }
    @Override public boolean meldrite$isTracked() { return valid; }
    @Override public void meldrite$setTracked(boolean value) { valid = value; }
    @Override public CraftEntity meldrite$getCachedBukkitEntity() { return meldrite$bukkitEntity; }
    @Override public void meldrite$setCachedBukkitEntity(CraftEntity entity) { meldrite$bukkitEntity = entity; }
    @Override public int meldrite$getMaximumNoDamageTicks() { return meldrite$maximumNoDamageTicks; }
    @Override public void meldrite$setMaximumNoDamageTicks(int ticks) { meldrite$maximumNoDamageTicks = ticks; }
    @Override public org.bukkit.event.entity.EntityDamageEvent meldrite$getLastDamageCause() { return meldrite$lastDamageCause; }
    @Override public void meldrite$setLastDamageCause(org.bukkit.event.entity.EntityDamageEvent event) { meldrite$lastDamageCause = event; }
    @Override public void meldrite$setLastDamageCancelled(boolean cancelled) { lastDamageCancelled = cancelled; }
    @Override public boolean meldrite$hasMaxAirOverride() { return meldrite$maxAirOverridden; }
    @Override public int meldrite$getMaxAirOverride() { return meldrite$maxAirOverride; }
    @Override public void meldrite$setMaxAirOverride(int ticks) { meldrite$maxAirOverride = ticks; meldrite$maxAirOverridden = true; }
    @Override public boolean meldrite$isCollidable() { return meldrite$collidable; }
    @Override public void meldrite$setCollidable(boolean collidable) { meldrite$collidable = collidable; meldrite$collidableOverridden = true; }
    @Override public Set<UUID> meldrite$getCollidableExemptions() {
        if (meldrite$collidableExemptions == null) meldrite$collidableExemptions = new LinkedHashSet<>();
        return meldrite$collidableExemptions;
    }
    @Override public TriState meldrite$getFrictionState() { return meldrite$frictionState; }
    @Override public void meldrite$setFrictionState(TriState state) { meldrite$frictionState = java.util.Objects.requireNonNull(state, "state"); }
    @Override public int meldrite$getShieldBlockingDelay() { return meldrite$shieldBlockingDelay; }
    @Override public void meldrite$setShieldBlockingDelay(int delay) { meldrite$shieldBlockingDelay = delay; }
    @Override public boolean meldrite$isFixedPose() { return meldrite$fixedPose; }
    @Override public void meldrite$setFixedPose(boolean fixed) { meldrite$fixedPose = fixed; }
    @Override public boolean meldrite$hasVisualFire() { return meldrite$visualFire; }
    @Override public void meldrite$setVisualFire(boolean visualFire) { meldrite$visualFire = visualFire; }
    @Override public synchronized void meldrite$setMetadata(String key, MetadataValue value) {
        if (meldrite$metadata == null) meldrite$metadata = new HashMap<>();
        if (value == null || value.getOwningPlugin() == null) throw new IllegalArgumentException("Metadata value must have an owning plugin");
        synchronized (meldrite$metadata) {
            meldrite$metadata.computeIfAbsent(key, ignored -> new IdentityHashMap<>()).put(value.getOwningPlugin(), value);
        }
    }
    @Override public synchronized List<MetadataValue> meldrite$getMetadata(String key) {
        if (meldrite$metadata == null) return List.of();
        synchronized (meldrite$metadata) {
            var values = meldrite$metadata.get(key);
            return values == null ? List.of() : List.copyOf(values.values());
        }
    }
    @Override public synchronized boolean meldrite$hasMetadata(String key) {
        if (meldrite$metadata == null) return false;
        synchronized (meldrite$metadata) { return meldrite$metadata.containsKey(key) && !meldrite$metadata.get(key).isEmpty(); }
    }
    @Override public synchronized void meldrite$removeMetadata(String key, Plugin plugin) {
        if (meldrite$metadata == null) return;
        synchronized (meldrite$metadata) {
            var values = meldrite$metadata.get(key);
            if (values == null) return;
            var removed = values.remove(plugin);
            if (removed != null) removed.invalidate();
            if (values.isEmpty()) meldrite$metadata.remove(key);
        }
    }
    @Override public CraftPersistentDataContainer meldrite$getPersistentDataContainer() {
        if (meldrite$persistentDataContainer == null) {
            meldrite$persistentDataContainer = new CraftPersistentDataContainer(MELDRITE_DATA_TYPES);
        }
        return meldrite$persistentDataContainer;
    }

    @WrapOperation(method = "dismountVehicle", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;removePassenger(Lnet/minecraft/entity/Entity;)V"), require = 1)
    private void meldrite$dismountEvent(Entity mount, Entity passenger, Operation<Void> original) {
        // The rider link is already null, so listener cleanup can remove the
        // mount without recursively emitting the same dismount event.
        if (!NativeEntityExtensions.allowDismount(passenger, mount) && vehicle == null
                && !passenger.isRemoved() && !mount.isRemoved() && mount.hasPassenger(passenger)) {
            vehicle = mount;
            return;
        }
        // A listener may remount the rider. Keep that relationship intact.
        if (vehicle != mount) original.call(mount, passenger);
    }

    @Inject(method = {"saveSelfNbt", "saveNbt"}, at = @At("HEAD"), cancellable = true)
    private void meldrite$skipNonPersistent(NbtCompound nbt, CallbackInfoReturnable<Boolean> result) {
        if (!persist) result.setReturnValue(false);
    }
    @Inject(method = "writeNbt", at = @At("RETURN"))
    private void meldrite$writePersistence(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> result) {
        if (!persist) result.getReturnValue().putBoolean("Bukkit.persist", false);
        if (meldrite$persistentDataContainer != null && !meldrite$persistentDataContainer.isEmpty()) {
            result.getReturnValue().put("BukkitValues", meldrite$persistentDataContainer.toTagCompound());
        }
    }
    @Inject(method = "readNbt", at = @At("TAIL"))
    private void meldrite$readPersistence(NbtCompound nbt, CallbackInfo result) {
        persist = !nbt.contains("Bukkit.persist") || nbt.getBoolean("Bukkit.persist");
        if (nbt.contains("BukkitValues", NbtElement.COMPOUND_TYPE)) {
            meldrite$getPersistentDataContainer().putAll(nbt.getCompound("BukkitValues"));
        }
    }
    @Inject(method = "setRemoved", at = @At("HEAD"))
    private void meldrite$removed(Entity.RemovalReason reason, CallbackInfo result) { valid = false; }
    @Inject(method = "getMaxAir", at = @At("HEAD"), cancellable = true)
    private void meldrite$maxAir(CallbackInfoReturnable<Integer> result) {
        if (meldrite$maxAirOverridden) result.setReturnValue(meldrite$maxAirOverride);
    }

    @Inject(method = "isCollidable", at = @At("HEAD"), cancellable = true)
    private void meldrite$isCollidable(CallbackInfoReturnable<Boolean> result) {
        if (meldrite$collidableOverridden) result.setReturnValue(meldrite$collidable);
    }

    @Inject(method = "collidesWith", at = @At("HEAD"), cancellable = true)
    private void meldrite$collidesWith(Entity other, CallbackInfoReturnable<Boolean> result) {
        PaperEntityAccess otherAccess = (PaperEntityAccess) other;
        if (meldrite$getCollidableExemptions().contains(other.getUuid())
                || otherAccess.meldrite$getCollidableExemptions().contains(((Entity) (Object) this).getUuid())) {
            result.setReturnValue(false);
        }
    }
}
