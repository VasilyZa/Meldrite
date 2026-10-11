package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.passive.SnifferEntity;

public class CraftSniffer extends CraftAnimals implements org.bukkit.entity.Sniffer {
    public CraftSniffer(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public SnifferEntity getHandle() { return (SnifferEntity) super.getHandle(); }
    private dev.meldrite.v1201.hook.SnifferEntityPaperAccessMixin snifferState() { return (dev.meldrite.v1201.hook.SnifferEntityPaperAccessMixin) getHandle(); }
    @Override public java.util.Collection<org.bukkit.Location> getExploredLocations() {
        return snifferState().meldrite$explored().map(pos -> {
            var world = ((net.minecraft.server.world.ServerWorld) getHandle().getWorld()).getServer().getWorld(pos.getDimension());
            var bukkitWorld = world == null ? null : dev.meldrite.v1201.compat.NativeEntityExtensions.world(world);
            return new org.bukkit.Location(bukkitWorld, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ());
        }).toList();
    }
    @Override public void removeExploredLocation(org.bukkit.Location location) {
        java.util.Objects.requireNonNull(location, "location").checkFinite(); requireWritable();
        var world = location.getWorld() == null ? getHandle().getWorld() : ((org.bukkit.craftbukkit.v1_20_R1.CraftWorld) location.getWorld()).getHandle();
        var global = net.minecraft.util.math.GlobalPos.create(world.getRegistryKey(), net.minecraft.util.math.BlockPos.ofFloored(location.getX(), location.getY(), location.getZ()));
        getHandle().getBrain().remember(net.minecraft.entity.ai.brain.MemoryModuleType.SNIFFER_EXPLORED_POSITIONS, snifferState().meldrite$explored().filter(pos -> !pos.equals(global)).toList());
    }
    @Override public void addExploredLocation(org.bukkit.Location location) {
        java.util.Objects.requireNonNull(location, "location").checkFinite(); requireWritable();
        if (location.getWorld() != getWorld()) return;
        snifferState().meldrite$explore(net.minecraft.util.math.BlockPos.ofFloored(location.getX(), location.getY(), location.getZ()));
    }
    @Override public State getState() { return State.valueOf(snifferState().meldrite$state().name()); }
    @Override public void setState(State state) { requireWritable(); getHandle().startState(SnifferEntity.State.valueOf(java.util.Objects.requireNonNull(state, "state").name())); }
    @Override public org.bukkit.Location findPossibleDigLocation() { return snifferState().meldrite$digLocation().map(pos -> new org.bukkit.Location(getWorld(), pos.getX(), pos.getY(), pos.getZ())).orElse(null); }
    @Override public boolean canDig() { return snifferState().meldrite$canDig(); }
}
