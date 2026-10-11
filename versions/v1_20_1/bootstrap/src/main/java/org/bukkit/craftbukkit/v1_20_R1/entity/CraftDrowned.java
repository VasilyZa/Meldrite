package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.mob.DrownedEntity;

public class CraftDrowned extends CraftZombie implements org.bukkit.entity.Drowned, com.destroystokyo.paper.entity.CraftRangedEntity<DrownedEntity> {
    public CraftDrowned(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public DrownedEntity getHandle() { return (DrownedEntity) super.getHandle(); }
}
