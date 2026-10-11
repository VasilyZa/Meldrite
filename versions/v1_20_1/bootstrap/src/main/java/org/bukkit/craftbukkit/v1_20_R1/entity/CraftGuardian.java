package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.compat.NativeEntityExtensions;
import dev.meldrite.v1201.hook.GuardianEntityPaperAccessMixin;
import net.minecraft.entity.mob.GuardianEntity;

public class CraftGuardian extends CraftMonster implements org.bukkit.entity.Guardian {
    private static final int MINIMUM_ATTACK_TICKS = -10;
    private int laserTicks = MINIMUM_ATTACK_TICKS;

    public CraftGuardian(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public GuardianEntity getHandle() { return (GuardianEntity) super.getHandle(); }
    private GuardianEntityPaperAccessMixin access() { return (GuardianEntityPaperAccessMixin) (Object) getHandle(); }
    @Override public void setTarget(org.bukkit.entity.LivingEntity target) {
        super.setTarget(target);
        if (target == null) { requireWritable(); access().meldrite$setBeamTarget(0); }
    }
    @Override public boolean setLaser(boolean activated) {
        requireWritable();
        if (!activated) { access().meldrite$setBeamTarget(0); return true; }
        org.bukkit.entity.LivingEntity target = getTarget();
        if (target == null) return false;
        access().meldrite$setBeamTarget(target.getEntityId());
        return true;
    }
    @Override public boolean hasLaser() { return getHandle().hasBeamTarget(); }
    @Override public int getLaserDuration() { return getHandle().getWarmupTime(); }
    @Override public void setLaserTicks(int ticks) {
        if (ticks < MINIMUM_ATTACK_TICKS) throw new IllegalArgumentException("ticks must be >= -10");
        requireWritable();
        laserTicks = ticks;
    }
    @Override public int getLaserTicks() { return laserTicks; }
    @Override public boolean isElder() { return false; }
    @Override public void setElder(boolean elder) {
        if (elder) throw new UnsupportedOperationException("Guardian cannot be converted to an elder guardian in place");
    }
    @Override public boolean isMoving() { return getHandle().getVelocity().lengthSquared() > 1.0E-7D; }
}
