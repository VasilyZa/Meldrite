package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.passive.SheepEntity;

public class CraftSheep extends CraftAnimals implements org.bukkit.entity.Sheep, io.papermc.paper.entity.PaperShearable {
    public CraftSheep(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public SheepEntity getHandle() { return (SheepEntity) super.getHandle(); }
    @Override public org.bukkit.DyeColor getColor() { return org.bukkit.DyeColor.getByWoolData((byte) getHandle().getColor().getId()); }
    @Override public void setColor(org.bukkit.DyeColor color) { requireWritable(); getHandle().setColor(net.minecraft.util.DyeColor.byId(java.util.Objects.requireNonNull(color, "color").getWoolData())); }
    @Override public boolean isSheared() { return getHandle().isSheared(); }
    @Override public void setSheared(boolean flag) { requireWritable(); getHandle().setSheared(flag); }
}
