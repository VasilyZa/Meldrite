package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.compat.ShulkerBulletNativeAccess;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.util.math.Direction;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

public final class CraftShulkerBullet extends CraftProjectile implements org.bukkit.entity.ShulkerBullet {
    public CraftShulkerBullet(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public ShulkerBulletEntity getHandle() { return (ShulkerBulletEntity) super.getHandle(); }
    private ShulkerBulletNativeAccess state() { return (ShulkerBulletNativeAccess) (Object) getHandle(); }
    @Override public org.bukkit.entity.Entity getTarget() { var target = state().meldrite$getTarget(); return target == null ? null : dev.meldrite.v1201.compat.NativeEntityExtensions.bukkit(target); }
    @Override public void setTarget(org.bukkit.entity.Entity target) { delegate.requireWritable(); state().meldrite$setTarget(target == null ? null : nativeEntity(target)); }
    @Override public Vector getTargetDelta() { return new Vector(state().meldrite$getTargetX(), state().meldrite$getTargetY(), state().meldrite$getTargetZ()); }
    @Override public void setTargetDelta(Vector vector) { java.util.Objects.requireNonNull(vector, "vector"); vector.checkFinite(); delegate.requireWritable(); state().meldrite$setTargetX(vector.getX()); state().meldrite$setTargetY(vector.getY()); state().meldrite$setTargetZ(vector.getZ()); }
    @Override public BlockFace getCurrentMovementDirection() { return BlockFace.valueOf(state().meldrite$getDirection().name()); }
    @Override public void setCurrentMovementDirection(BlockFace face) { java.util.Objects.requireNonNull(face); if (!face.isCartesian()) throw new IllegalArgumentException("movementDirection must be cartesian"); delegate.requireWritable(); state().meldrite$setDirection(Direction.valueOf(face.name())); }
    @Override public int getFlightSteps() { return state().meldrite$getFlightSteps(); }
    @Override public void setFlightSteps(int steps) { if (steps < 0) throw new IllegalArgumentException("flight steps must be nonnegative"); delegate.requireWritable(); state().meldrite$setFlightSteps(steps); }
    private net.minecraft.entity.Entity nativeEntity(org.bukkit.entity.Entity entity) { if (!(entity instanceof CraftEntity craft)) throw new IllegalArgumentException("FOREIGN_ENTITY_FACADE"); return craft.getHandle(); }
}
