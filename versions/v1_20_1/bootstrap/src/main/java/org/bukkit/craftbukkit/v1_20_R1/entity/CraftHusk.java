package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.mob.HuskEntity;

public class CraftHusk extends CraftZombie implements org.bukkit.entity.Husk {
    public CraftHusk(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public HuskEntity getHandle() { return (HuskEntity) super.getHandle(); }
}
