package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.passive.TraderLlamaEntity;

public class CraftTraderLlama extends CraftLlama implements org.bukkit.entity.TraderLlama {
    public CraftTraderLlama(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public TraderLlamaEntity getHandle() { return (TraderLlamaEntity) super.getHandle(); }
}
