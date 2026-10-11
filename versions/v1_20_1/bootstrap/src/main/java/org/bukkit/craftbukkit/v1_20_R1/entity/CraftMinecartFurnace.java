package org.bukkit.craftbukkit.v1_20_R1.entity;
import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.compat.FurnaceMinecartNativeAccess;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
public final class CraftMinecartFurnace extends CraftMinecart implements org.bukkit.entity.minecart.PoweredMinecart {
    public CraftMinecartFurnace(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public FurnaceMinecartEntity getHandle() { return (FurnaceMinecartEntity) super.getHandle(); }
    private FurnaceMinecartNativeAccess state() { return (FurnaceMinecartNativeAccess) (Object) getHandle(); }
    @Override public int getFuel() { return state().meldrite$getFuel(); }
    @Override public void setFuel(int fuel) { if (fuel < 0) throw new IllegalArgumentException("fuel must be nonnegative"); requireWritable(); state().meldrite$setFuel(fuel); }
    @Override public double getPushX() { return getHandle().pushX; }
    @Override public double getPushZ() { return getHandle().pushZ; }
    @Override public void setPushX(double value) { if (!Double.isFinite(value)) throw new IllegalArgumentException("pushX must be finite"); requireWritable(); getHandle().pushX = value; }
    @Override public void setPushZ(double value) { if (!Double.isFinite(value)) throw new IllegalArgumentException("pushZ must be finite"); requireWritable(); getHandle().pushZ = value; }
}
