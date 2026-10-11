package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.passive.ParrotEntity;

public class CraftParrot extends CraftTameableAnimal implements org.bukkit.entity.Parrot {
    public CraftParrot(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public ParrotEntity getHandle() { return (ParrotEntity) super.getHandle(); }
    @Override public Variant getVariant() { return Variant.values()[getHandle().getVariant().ordinal()]; }
    @Override public void setVariant(Variant variant) { requireWritable(); getHandle().setVariant(ParrotEntity.Variant.values()[java.util.Objects.requireNonNull(variant, "variant").ordinal()]); }
    @Override public boolean isDancing() { return getHandle().isSongPlaying(); }
}
