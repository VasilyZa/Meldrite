package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.passive.AbstractDonkeyEntity;

public class CraftChestedHorse extends CraftAbstractHorse implements org.bukkit.entity.ChestedHorse {
    public CraftChestedHorse(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public AbstractDonkeyEntity getHandle() { return (AbstractDonkeyEntity) super.getHandle(); }
    @Override public boolean isCarryingChest() { return getHandle().hasChest(); }
    @Override public void setCarryingChest(boolean value) { requireWritable(); if (value != isCarryingChest()) { getHandle().setHasChest(value); horseAccess().meldrite$resizeInventory(); } }
}
