package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.mob.CaveSpiderEntity;

public final class CraftCaveSpider extends CraftSpider implements org.bukkit.entity.CaveSpider {
    public CraftCaveSpider(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public CaveSpiderEntity getHandle() { return (CaveSpiderEntity) super.getHandle(); }
}
