package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.mob.IllusionerEntity;

public class CraftIllusioner extends CraftSpellcaster implements org.bukkit.entity.Illusioner, com.destroystokyo.paper.entity.CraftRangedEntity<IllusionerEntity> {
    public CraftIllusioner(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public IllusionerEntity getHandle() { return (IllusionerEntity) super.getHandle(); }
}
