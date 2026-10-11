package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.mob.SpiderEntity;

public class CraftSpider extends CraftMonster implements org.bukkit.entity.Spider {
    public CraftSpider(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public SpiderEntity getHandle() { return (SpiderEntity) super.getHandle(); }
}
