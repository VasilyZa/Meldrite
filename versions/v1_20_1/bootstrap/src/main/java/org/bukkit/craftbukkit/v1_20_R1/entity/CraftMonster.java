package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.mob.HostileEntity;

/** Shared Bukkit Monster projection for native hostile mobs. */
public class CraftMonster extends CraftCreature implements org.bukkit.entity.Monster, CraftEnemy {
    public CraftMonster(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public HostileEntity getHandle() { return (HostileEntity) super.getHandle(); }
}
