package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.hook.RavagerEntityPaperAccessMixin;
import net.minecraft.entity.mob.RavagerEntity;

public class CraftRavager extends CraftRaider implements org.bukkit.entity.Ravager {
    public CraftRavager(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public RavagerEntity getHandle() { return (RavagerEntity) super.getHandle(); }
    @Override public int getAttackTicks() { return getHandle().getAttackTick(); }
    @Override public void setAttackTicks(int ticks) { requireWritable(); ((RavagerEntityPaperAccessMixin) getHandle()).meldrite$setAttackTicks(ticks); }
    @Override public int getStunnedTicks() { return getHandle().getStunTick(); }
    @Override public void setStunnedTicks(int ticks) { requireWritable(); ((RavagerEntityPaperAccessMixin) getHandle()).meldrite$setStunTicks(ticks); }
    @Override public int getRoarTicks() { return getHandle().getRoarTick(); }
    @Override public void setRoarTicks(int ticks) { requireWritable(); ((RavagerEntityPaperAccessMixin) getHandle()).meldrite$setRoarTicks(ticks); }
}
