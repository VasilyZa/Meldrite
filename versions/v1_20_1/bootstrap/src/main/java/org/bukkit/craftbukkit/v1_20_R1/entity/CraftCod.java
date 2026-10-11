package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.passive.CodEntity;

public class CraftCod extends io.papermc.paper.entity.PaperSchoolableFish implements org.bukkit.entity.Cod {
    public CraftCod(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public CodEntity getHandle() { return (CodEntity) super.getHandle(); }
}
