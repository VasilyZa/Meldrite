package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.passive.PufferfishEntity;

public class CraftPufferFish extends CraftFish implements org.bukkit.entity.PufferFish {
    public CraftPufferFish(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public PufferfishEntity getHandle() { return (PufferfishEntity) super.getHandle(); }
    @Override public int getPuffState() { return getHandle().getPuffState(); }
    @Override public void setPuffState(int state) { if (state < 0 || state > 2) throw new IllegalArgumentException("Puff state must be 0, 1 or 2"); requireWritable(); getHandle().setPuffState(state); }
}
