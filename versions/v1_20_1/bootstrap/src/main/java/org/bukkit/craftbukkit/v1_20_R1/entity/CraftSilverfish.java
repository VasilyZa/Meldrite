package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.mob.SilverfishEntity;

public final class CraftSilverfish extends CraftMonster implements org.bukkit.entity.Silverfish {
    public CraftSilverfish(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public SilverfishEntity getHandle() { return (SilverfishEntity) super.getHandle(); }
}
