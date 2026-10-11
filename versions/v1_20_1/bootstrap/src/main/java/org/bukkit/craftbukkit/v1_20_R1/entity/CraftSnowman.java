package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.passive.SnowGolemEntity;

public class CraftSnowman extends CraftGolem implements org.bukkit.entity.Snowman, com.destroystokyo.paper.entity.CraftRangedEntity<SnowGolemEntity>, io.papermc.paper.entity.PaperShearable {
    public CraftSnowman(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public SnowGolemEntity getHandle() { return (SnowGolemEntity) super.getHandle(); }
    @Override public boolean isDerp() { return !getHandle().hasPumpkin(); }
    @Override public void setDerp(boolean value) { requireWritable(); getHandle().setHasPumpkin(!value); }
}
