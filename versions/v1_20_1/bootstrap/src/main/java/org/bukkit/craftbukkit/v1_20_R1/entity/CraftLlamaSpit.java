package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.projectile.LlamaSpitEntity;

public final class CraftLlamaSpit extends CraftProjectile implements org.bukkit.entity.LlamaSpit {
    public CraftLlamaSpit(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public LlamaSpitEntity getHandle() { return (LlamaSpitEntity) super.getHandle(); }
}
