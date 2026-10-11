package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.passive.OcelotEntity;

public class CraftOcelot extends CraftAnimals implements org.bukkit.entity.Ocelot {
    public CraftOcelot(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public OcelotEntity getHandle() { return (OcelotEntity) super.getHandle(); }
    private dev.meldrite.v1201.hook.OcelotEntityPaperAccessMixin state() { return (dev.meldrite.v1201.hook.OcelotEntityPaperAccessMixin) getHandle(); }
    @Override public boolean isTrusting() { return state().meldrite$isTrusting(); }
    @Override public void setTrusting(boolean trusting) { requireWritable(); state().meldrite$setTrusting(trusting); }
    @Override public Type getCatType() { return Type.WILD_OCELOT; }
    @Override public void setCatType(Type type) { throw new UnsupportedOperationException("Cats are now a different entity!"); }
}
