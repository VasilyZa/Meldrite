package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.projectile.WitherSkullEntity;

public class CraftWitherSkull extends CraftFireball implements org.bukkit.entity.WitherSkull {
    public CraftWitherSkull(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public WitherSkullEntity getHandle() { return (WitherSkullEntity) super.getHandle(); }
    @Override public boolean isCharged() { return getHandle().isCharged(); }
    @Override public void setCharged(boolean charged) { delegate.requireWritable(); getHandle().setCharged(charged); }
}
