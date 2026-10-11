package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.compat.ExplosiveProjectileNativeAccess;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import org.bukkit.util.Vector;

/** CraftBukkit facade for the native explosive projectile family. */
public class CraftFireball extends CraftProjectile implements org.bukkit.entity.Fireball {
    public CraftFireball(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public ExplosiveProjectileEntity getHandle() { return (ExplosiveProjectileEntity) super.getHandle(); }
    private ExplosiveProjectileNativeAccess state() { return (ExplosiveProjectileNativeAccess) (Object) getHandle(); }
    @Override public float getYield() { return state().meldrite$getYield(); }
    @Override public void setYield(float yield) { if (!Float.isFinite(yield) || yield < 0) throw new IllegalArgumentException("yield must be finite and nonnegative"); delegate.requireWritable(); state().meldrite$setYield(yield); }
    @Override public boolean isIncendiary() { return state().meldrite$isIncendiary(); }
    @Override public void setIsIncendiary(boolean incendiary) { delegate.requireWritable(); state().meldrite$setIncendiary(incendiary); }
    @Override public Vector getDirection() { return new Vector(getHandle().powerX, getHandle().powerY, getHandle().powerZ); }
    @Override public void setDirection(Vector direction) { java.util.Objects.requireNonNull(direction, "direction"); direction.checkFinite(); delegate.requireWritable(); getHandle().powerX = direction.getX(); getHandle().powerY = direction.getY(); getHandle().powerZ = direction.getZ(); }
}
