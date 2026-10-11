package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.mob.MagmaCubeEntity;

public class CraftMagmaCube extends CraftSlime implements org.bukkit.entity.MagmaCube {
    public CraftMagmaCube(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public MagmaCubeEntity getHandle() { return (MagmaCubeEntity) super.getHandle(); }
}
