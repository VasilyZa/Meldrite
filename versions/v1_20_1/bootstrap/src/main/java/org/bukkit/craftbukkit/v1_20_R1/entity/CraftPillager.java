package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.mob.PillagerEntity;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventory;

public class CraftPillager extends CraftIllager implements org.bukkit.entity.Pillager, com.destroystokyo.paper.entity.CraftRangedEntity<PillagerEntity> {
    public CraftPillager(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public PillagerEntity getHandle() { return (PillagerEntity) super.getHandle(); }
    @Override public org.bukkit.inventory.Inventory getInventory() { return new CraftInventory(getHandle().getInventory(), this); }
}
