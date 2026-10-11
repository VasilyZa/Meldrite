package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.hook.GhastEntityPaperAccessMixin;
import net.minecraft.entity.mob.GhastEntity;

public final class CraftGhast extends CraftFlying implements org.bukkit.entity.Ghast, CraftEnemy {
    public CraftGhast(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public GhastEntity getHandle() { return (GhastEntity) super.getHandle(); }
    private GhastEntityPaperAccessMixin access() { return (GhastEntityPaperAccessMixin) (Object) getHandle(); }
    @Override public boolean isCharging() { return getHandle().isShooting(); }
    @Override public void setCharging(boolean charging) { requireWritable(); getHandle().setShooting(charging); }
    @Override public int getExplosionPower() { return access().meldrite$getFireballStrength(); }
    @Override public void setExplosionPower(int power) { if (power < 0) throw new IllegalArgumentException("power must be nonnegative"); requireWritable(); access().meldrite$setFireballStrength(power); }
}
