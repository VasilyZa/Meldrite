package org.bukkit.craftbukkit.v1_20_R1.entity;
import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.compat.TntNativeAccess;
import net.minecraft.entity.TntEntity;
public final class CraftTNTPrimed extends CraftEntity implements org.bukkit.entity.TNTPrimed {
    public CraftTNTPrimed(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public TntEntity getHandle() { return (TntEntity) super.getHandle(); }
    private void requireWritable() { delegate.requireWritable(); }
    private TntNativeAccess state() { return (TntNativeAccess) (Object) getHandle(); }
    @Override public float getYield() { return state().meldrite$getYield(); }
    @Override public void setYield(float yield) { if (!Float.isFinite(yield) || yield < 0) throw new IllegalArgumentException("yield must be finite and nonnegative"); requireWritable(); state().meldrite$setYield(yield); }
    @Override public boolean isIncendiary() { return state().meldrite$isIncendiary(); }
    @Override public void setIsIncendiary(boolean incendiary) { requireWritable(); state().meldrite$setIncendiary(incendiary); }
    @Override public int getFuseTicks() { return getHandle().getFuse(); }
    @Override public void setFuseTicks(int ticks) { if (ticks < 0) throw new IllegalArgumentException("fuse ticks must be nonnegative"); requireWritable(); getHandle().setFuse(ticks); }
    @Override public org.bukkit.entity.Entity getSource() { var source = getHandle().getOwner(); return source == null ? null : dev.meldrite.v1201.compat.NativeEntityExtensions.bukkit(source); }
    @Override public void setSource(org.bukkit.entity.Entity source) { requireWritable(); if (source == null) { state().meldrite$setSource(null); return; } if (!(source instanceof CraftEntity craft) || !(craft.getHandle() instanceof net.minecraft.entity.LivingEntity living)) throw new IllegalArgumentException("source must be a live living CraftEntity"); state().meldrite$setSource(living); }
}
