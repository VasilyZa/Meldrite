package org.bukkit.craftbukkit.v1_20_R1.entity;
import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.passive.PolarBearEntity;
public class CraftPolarBear extends CraftAnimals implements org.bukkit.entity.PolarBear {
    public CraftPolarBear(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public PolarBearEntity getHandle() { return (PolarBearEntity) super.getHandle(); }
    @Override public boolean isStanding() { return getHandle().isWarning(); }
    @Override public void setStanding(boolean value) { requireWritable(); getHandle().setWarning(value); }
}
