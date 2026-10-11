package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.mob.BlazeEntity;

public final class CraftBlaze extends CraftMonster implements org.bukkit.entity.Blaze {
    public CraftBlaze(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public BlazeEntity getHandle() { return (BlazeEntity) super.getHandle(); }
}
