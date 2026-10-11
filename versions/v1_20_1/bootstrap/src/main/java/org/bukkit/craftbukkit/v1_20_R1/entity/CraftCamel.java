package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.passive.CamelEntity;

public class CraftCamel extends CraftAbstractHorse implements org.bukkit.entity.Camel {
    public CraftCamel(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public CamelEntity getHandle() { return (CamelEntity) super.getHandle(); }
    @Override public org.bukkit.entity.Horse.Variant getVariant() { return org.bukkit.entity.Horse.Variant.CAMEL; }
    @Override public boolean isDashing() { return getHandle().isDashing(); }
    @Override public void setDashing(boolean value) { requireWritable(); getHandle().setDashing(value); }
    @Override public boolean isSitting() { return getHandle().getPose() == EntityPose.SITTING; }
    @Override public void setSitting(boolean value) { requireWritable(); if (value) getHandle().startSitting(); else getHandle().startStanding(); }
}
