package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;

public abstract class CraftVehicle extends CraftEntity implements org.bukkit.entity.Vehicle {
    protected CraftVehicle(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    protected final void requireWritable() { delegate.requireWritable(); }
}
