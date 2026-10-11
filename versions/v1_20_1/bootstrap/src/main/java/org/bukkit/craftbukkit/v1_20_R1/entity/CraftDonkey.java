package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.passive.DonkeyEntity;

public class CraftDonkey extends CraftChestedHorse implements org.bukkit.entity.Donkey {
    public CraftDonkey(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public DonkeyEntity getHandle() { return (DonkeyEntity) super.getHandle(); }
    @Override public org.bukkit.entity.Horse.Variant getVariant() { return org.bukkit.entity.Horse.Variant.DONKEY; }
}
