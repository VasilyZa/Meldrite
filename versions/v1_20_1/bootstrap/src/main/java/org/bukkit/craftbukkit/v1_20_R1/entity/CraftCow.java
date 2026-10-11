package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.passive.CowEntity;

public class CraftCow extends CraftAnimals implements org.bukkit.entity.Cow {
    public CraftCow(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public CowEntity getHandle() { return (CowEntity) super.getHandle(); }

}
