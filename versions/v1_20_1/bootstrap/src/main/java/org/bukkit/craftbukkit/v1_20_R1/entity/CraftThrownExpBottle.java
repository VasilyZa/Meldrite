package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;

public final class CraftThrownExpBottle extends CraftThrowableProjectile implements org.bukkit.entity.ThrownExpBottle {
    public CraftThrownExpBottle(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public ExperienceBottleEntity getHandle() { return (ExperienceBottleEntity) super.getHandle(); }
}
