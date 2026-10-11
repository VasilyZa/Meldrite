package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.passive.GolemEntity;

public class CraftGolem extends CraftCreature implements org.bukkit.entity.Golem {
    public CraftGolem(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public GolemEntity getHandle() { return (GolemEntity) super.getHandle(); }
}
