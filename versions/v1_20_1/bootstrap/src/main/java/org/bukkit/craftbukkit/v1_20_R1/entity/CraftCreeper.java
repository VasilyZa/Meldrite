package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.hook.CreeperEntityPaperAccessMixin;
import net.minecraft.entity.mob.CreeperEntity;

public class CraftCreeper extends CraftMonster implements org.bukkit.entity.Creeper {
    public CraftCreeper(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public CreeperEntity getHandle() { return (CreeperEntity) super.getHandle(); }
    private CreeperEntityPaperAccessMixin state() { return (CreeperEntityPaperAccessMixin) (Object) getHandle(); }
    @Override public boolean isPowered() { return getHandle().getDataTracker().get(CreeperEntityPaperAccessMixin.meldrite$charged()); }
    @Override public void setPowered(boolean value) { requireWritable(); getHandle().getDataTracker().set(CreeperEntityPaperAccessMixin.meldrite$charged(), value); }
    @Override public void setMaxFuseTicks(int ticks) { if (ticks < 0) throw new IllegalArgumentException("ticks < 0"); requireWritable(); state().meldrite$setFuseTime(ticks); }
    @Override public int getMaxFuseTicks() { return state().meldrite$getFuseTime(); }
    @Override public void setFuseTicks(int ticks) { if (ticks < 0 || ticks > getMaxFuseTicks()) throw new IllegalArgumentException("ticks outside fuse range"); requireWritable(); state().meldrite$setCurrentFuseTime(ticks); }
    @Override public int getFuseTicks() { return state().meldrite$getCurrentFuseTime(); }
    @Override public void setExplosionRadius(int radius) { if (radius < 0) throw new IllegalArgumentException("radius < 0"); requireWritable(); state().meldrite$setExplosionRadius(radius); }
    @Override public int getExplosionRadius() { return state().meldrite$getExplosionRadius(); }
    @Override public void explode() { requireWritable(); state().meldrite$explode(); }
    @Override public void ignite() { requireWritable(); getHandle().ignite(); }
    @Override public void setIgnited(boolean value) { requireWritable(); getHandle().getDataTracker().set(CreeperEntityPaperAccessMixin.meldrite$ignited(), value); }
    @Override public boolean isIgnited() { return getHandle().isIgnited(); }
}
