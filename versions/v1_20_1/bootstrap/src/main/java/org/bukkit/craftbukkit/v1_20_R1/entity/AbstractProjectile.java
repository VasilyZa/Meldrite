package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.compat.NativeEntityExtensions;
import dev.meldrite.v1201.hook.ProjectileEntityAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.hit.EntityHitResult;

public abstract class AbstractProjectile extends CraftEntity implements org.bukkit.entity.Projectile {
    private boolean doesBounce;
    public AbstractProjectile(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public ProjectileEntity getHandle() { return (ProjectileEntity) super.getHandle(); }
    private ProjectileEntityAccess state() { return (ProjectileEntityAccess) getHandle(); }
    @Override public org.bukkit.projectiles.ProjectileSource getShooter() {
        Entity owner = getHandle().getOwner();
        return owner == null ? ((dev.meldrite.v1201.compat.NativeProjectileSource) getHandle()).meldrite$getSource() : NativeEntityExtensions.bukkit(owner);
    }
    @Override public void setShooter(org.bukkit.projectiles.ProjectileSource shooter) {
        delegate.requireWritable();
        ((dev.meldrite.v1201.compat.NativeProjectileSource) getHandle()).meldrite$setSource(shooter);
        if (shooter instanceof CraftEntity craft) getHandle().setOwner(craft.getHandle());
        else { state().meldrite$setOwner(null); state().meldrite$setOwnerUuid(null); }
    }
    @Override public boolean doesBounce() { return doesBounce; }
    @Override public void setBounce(boolean bounce) { delegate.requireWritable(); doesBounce = bounce; }
    @Override public boolean hasLeftShooter() { return state().meldrite$hasLeftOwner(); }
    @Override public void setHasLeftShooter(boolean value) { delegate.requireWritable(); state().meldrite$setLeftOwner(value); }
    @Override public boolean hasBeenShot() { return state().meldrite$hasBeenShot(); }
    @Override public void setHasBeenShot(boolean value) { delegate.requireWritable(); state().meldrite$setBeenShot(value); }
    private Entity nativeEntity(org.bukkit.entity.Entity entity) {
        if (!(entity instanceof CraftEntity craft)) throw new IllegalArgumentException("FOREIGN_ENTITY_FACADE");
        return craft.getHandle();
    }
    @Override public boolean canHitEntity(org.bukkit.entity.Entity entity) { return state().meldrite$canHit(nativeEntity(entity)); }
    @Override public void hitEntity(org.bukkit.entity.Entity entity) { hitEntity(entity, getLocation().toVector()); }
    @Override public void hitEntity(org.bukkit.entity.Entity entity, org.bukkit.util.Vector vector) {
        delegate.requireWritable();
        java.util.Objects.requireNonNull(vector, "vector").checkFinite();
        state().meldrite$onCollision(new EntityHitResult(nativeEntity(entity), new net.minecraft.util.math.Vec3d(vector.getX(), vector.getY(), vector.getZ())));
    }
    @Override public java.util.UUID getOwnerUniqueId() { return state().meldrite$getOwnerUuid(); }
}
