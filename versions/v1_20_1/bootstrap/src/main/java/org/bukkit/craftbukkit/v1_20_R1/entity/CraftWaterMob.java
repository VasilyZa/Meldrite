package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.mob.WaterCreatureEntity;

public class CraftWaterMob extends CraftCreature implements org.bukkit.entity.WaterMob {
    public CraftWaterMob(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public WaterCreatureEntity getHandle() { return (WaterCreatureEntity) super.getHandle(); }
}
