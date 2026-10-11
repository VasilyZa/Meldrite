package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.compat.FireballNativeAccess;
import net.minecraft.entity.projectile.FireballEntity;

public final class CraftLargeFireball extends CraftSizedFireball implements org.bukkit.entity.LargeFireball {
    public CraftLargeFireball(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public FireballEntity getHandle() { return (FireballEntity) super.getHandle(); }
    private FireballNativeAccess state() { return (FireballNativeAccess) (Object) getHandle(); }
    @Override public void setYield(float yield) { super.setYield(yield); state().meldrite$setExplosionPower(Math.max(0, (int) yield)); }
}
