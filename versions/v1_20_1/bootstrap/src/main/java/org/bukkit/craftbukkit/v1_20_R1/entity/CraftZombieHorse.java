package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.mob.ZombieHorseEntity;

public class CraftZombieHorse extends CraftAbstractHorse implements org.bukkit.entity.ZombieHorse {
    public CraftZombieHorse(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public ZombieHorseEntity getHandle() { return (ZombieHorseEntity) super.getHandle(); }
    @Override public org.bukkit.entity.Horse.Variant getVariant() { return org.bukkit.entity.Horse.Variant.UNDEAD_HORSE; }
}
