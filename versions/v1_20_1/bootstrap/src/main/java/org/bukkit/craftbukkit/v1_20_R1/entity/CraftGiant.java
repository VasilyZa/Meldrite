package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.mob.GiantEntity;

public final class CraftGiant extends CraftMonster implements org.bukkit.entity.Giant {
    public CraftGiant(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public GiantEntity getHandle() { return (GiantEntity) super.getHandle(); }
}
