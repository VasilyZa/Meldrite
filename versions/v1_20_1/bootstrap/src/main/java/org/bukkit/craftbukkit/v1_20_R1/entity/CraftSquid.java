package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.passive.SquidEntity;

public class CraftSquid extends CraftWaterMob implements org.bukkit.entity.Squid {
    public CraftSquid(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public SquidEntity getHandle() { return (SquidEntity) super.getHandle(); }
}
