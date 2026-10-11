package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.projectile.DragonFireballEntity;

public final class CraftDragonFireball extends CraftFireball implements org.bukkit.entity.DragonFireball {
    public CraftDragonFireball(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public DragonFireballEntity getHandle() { return (DragonFireballEntity) super.getHandle(); }
}
