package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.passive.IronGolemEntity;

public class CraftIronGolem extends CraftGolem implements org.bukkit.entity.IronGolem {
    public CraftIronGolem(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public IronGolemEntity getHandle() { return (IronGolemEntity) super.getHandle(); }
    @Override public boolean isPlayerCreated() { return getHandle().isPlayerCreated(); }
    @Override public void setPlayerCreated(boolean value) { requireWritable(); getHandle().setPlayerCreated(value); }
}
