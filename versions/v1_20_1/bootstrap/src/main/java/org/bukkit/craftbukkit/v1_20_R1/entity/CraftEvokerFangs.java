package org.bukkit.craftbukkit.v1_20_R1.entity;
import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
public class CraftEvokerFangs extends CraftEntity implements org.bukkit.entity.EvokerFangs {
    public CraftEvokerFangs(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public net.minecraft.entity.mob.EvokerFangsEntity getHandle() { return (net.minecraft.entity.mob.EvokerFangsEntity) super.getHandle(); }
    @Override public org.bukkit.entity.LivingEntity getOwner() { var owner = getHandle().getOwner(); return owner == null ? null : (org.bukkit.entity.LivingEntity) dev.meldrite.v1201.compat.NativeEntityExtensions.bukkit(owner); }
    @Override public void setOwner(org.bukkit.entity.LivingEntity owner) { delegate.requireWritable(); getHandle().setOwner(owner == null ? null : ((CraftLivingEntity) owner).getHandle()); }
}
