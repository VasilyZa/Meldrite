package org.bukkit.craftbukkit.v1_20_R1.entity;
import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.compat.TntMinecartNativeAccess;
import net.minecraft.entity.vehicle.TntMinecartEntity;
public final class CraftMinecartTNT extends CraftMinecart implements org.bukkit.entity.minecart.ExplosiveMinecart {
    public CraftMinecartTNT(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public TntMinecartEntity getHandle() { return (TntMinecartEntity) super.getHandle(); }
    private TntMinecartNativeAccess state() { return (TntMinecartNativeAccess) (Object) getHandle(); }
    @Override public void setFuseTicks(int ticks) { if (ticks < -1) throw new IllegalArgumentException("fuse ticks must be >= -1"); requireWritable(); state().meldrite$setFuseTicks(ticks); }
    @Override public int getFuseTicks() { return state().meldrite$getFuseTicks(); }
    @Override public void ignite() { requireWritable(); getHandle().prime(); }
    @Override public boolean isIgnited() { return getHandle().isPrimed(); }
    @Override public void explode() { explode(4.0); }
    @Override public void explode(double power) { if (!Double.isFinite(power) || power < 0) throw new IllegalArgumentException("power must be finite and nonnegative"); requireWritable(); getHandle().prime(); state().meldrite$setFuseTicks(0); }
}
