package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.mob.IllagerEntity;

public abstract class CraftIllager extends CraftRaider implements org.bukkit.entity.Illager {
    public CraftIllager(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public IllagerEntity getHandle() { return (IllagerEntity) super.getHandle(); }
}
