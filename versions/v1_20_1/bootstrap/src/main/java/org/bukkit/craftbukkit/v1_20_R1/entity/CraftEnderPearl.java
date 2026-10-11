package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;

public final class CraftEnderPearl extends CraftThrowableProjectile implements org.bukkit.entity.EnderPearl {
    public CraftEnderPearl(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public EnderPearlEntity getHandle() { return (EnderPearlEntity) super.getHandle(); }
}
