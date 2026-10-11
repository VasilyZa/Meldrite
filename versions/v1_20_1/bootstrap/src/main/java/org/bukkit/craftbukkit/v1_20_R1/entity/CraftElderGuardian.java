package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.mob.ElderGuardianEntity;

public final class CraftElderGuardian extends CraftGuardian implements org.bukkit.entity.ElderGuardian {
    public CraftElderGuardian(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public ElderGuardianEntity getHandle() { return (ElderGuardianEntity) super.getHandle(); }
    @Override public boolean isElder() { return true; }
}
