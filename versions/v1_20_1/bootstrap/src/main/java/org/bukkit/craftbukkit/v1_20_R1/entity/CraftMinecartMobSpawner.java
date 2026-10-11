package org.bukkit.craftbukkit.v1_20_R1.entity;
import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
public final class CraftMinecartMobSpawner extends CraftMinecart implements org.bukkit.entity.minecart.SpawnerMinecart {
    public CraftMinecartMobSpawner(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public net.minecraft.entity.vehicle.SpawnerMinecartEntity getHandle() { return (net.minecraft.entity.vehicle.SpawnerMinecartEntity) super.getHandle(); }
}
