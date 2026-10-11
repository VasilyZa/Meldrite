package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.mob.PathAwareEntity;

public class CraftCreature extends CraftMob implements org.bukkit.entity.Creature {
    public CraftCreature(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public PathAwareEntity getHandle() { return (PathAwareEntity) super.getHandle(); }
}
