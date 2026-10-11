package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.projectile.thrown.SnowballEntity;

public final class CraftSnowball extends CraftThrowableProjectile implements org.bukkit.entity.Snowball {
    public CraftSnowball(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public SnowballEntity getHandle() { return (SnowballEntity) super.getHandle(); }
}
