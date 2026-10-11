package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.compat.BoatNativeAccess;
import net.minecraft.entity.vehicle.BoatEntity;
import org.bukkit.TreeSpecies;

public class CraftBoat extends CraftVehicle implements org.bukkit.entity.Boat {
    public CraftBoat(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public BoatEntity getHandle() { return (BoatEntity) super.getHandle(); }
    private BoatNativeAccess state() { return (BoatNativeAccess) (Object) getHandle(); }
    private static org.bukkit.entity.Boat.Type bukkitType(BoatEntity.Type type) { return org.bukkit.entity.Boat.Type.valueOf(type.name()); }
    private static BoatEntity.Type nativeType(org.bukkit.entity.Boat.Type type) { return BoatEntity.Type.valueOf(type.name()); }
    @Override public TreeSpecies getWoodType() { return switch (getBoatType()) { case OAK -> TreeSpecies.GENERIC; case SPRUCE -> TreeSpecies.REDWOOD; case BIRCH -> TreeSpecies.BIRCH; case JUNGLE -> TreeSpecies.JUNGLE; case ACACIA -> TreeSpecies.ACACIA; case DARK_OAK -> TreeSpecies.DARK_OAK; default -> TreeSpecies.GENERIC; }; }
    @Override public void setWoodType(TreeSpecies species) { org.bukkit.entity.Boat.Type type = switch (java.util.Objects.requireNonNull(species)) { case REDWOOD -> org.bukkit.entity.Boat.Type.SPRUCE; case BIRCH -> org.bukkit.entity.Boat.Type.BIRCH; case JUNGLE -> org.bukkit.entity.Boat.Type.JUNGLE; case ACACIA -> org.bukkit.entity.Boat.Type.ACACIA; case DARK_OAK -> org.bukkit.entity.Boat.Type.DARK_OAK; default -> org.bukkit.entity.Boat.Type.OAK; }; setBoatType(type); }
    @Override public org.bukkit.entity.Boat.Type getBoatType() { return bukkitType(getHandle().getVariant()); }
    @Override public void setBoatType(org.bukkit.entity.Boat.Type type) { requireWritable(); getHandle().setVariant(nativeType(java.util.Objects.requireNonNull(type))); }
    @Override public double getMaxSpeed() { return state().meldrite$getMaxSpeed(); }
    @Override public void setMaxSpeed(double speed) { if (!Double.isFinite(speed) || speed < 0) throw new IllegalArgumentException("speed must be finite and nonnegative"); requireWritable(); state().meldrite$setMaxSpeed(speed); }
    @Override public double getOccupiedDeceleration() { return state().meldrite$getOccupiedDeceleration(); }
    @Override public void setOccupiedDeceleration(double speed) { if (!Double.isFinite(speed) || speed < 0) throw new IllegalArgumentException("speed must be finite and nonnegative"); requireWritable(); state().meldrite$setOccupiedDeceleration(speed); }
    @Override public double getUnoccupiedDeceleration() { return state().meldrite$getUnoccupiedDeceleration(); }
    @Override public void setUnoccupiedDeceleration(double speed) { if (!Double.isFinite(speed)) throw new IllegalArgumentException("speed must be finite"); requireWritable(); state().meldrite$setUnoccupiedDeceleration(speed); }
    @Override public boolean getWorkOnLand() { return state().meldrite$getLandBoats(); }
    @Override public void setWorkOnLand(boolean workOnLand) { requireWritable(); state().meldrite$setLandBoats(workOnLand); }
    @Override public org.bukkit.entity.Boat.Status getStatus() { return switch (state().meldrite$getLocation()) { case IN_WATER -> org.bukkit.entity.Boat.Status.IN_WATER; case UNDER_WATER -> org.bukkit.entity.Boat.Status.UNDER_WATER; case UNDER_FLOWING_WATER -> org.bukkit.entity.Boat.Status.UNDER_FLOWING_WATER; case ON_LAND -> org.bukkit.entity.Boat.Status.ON_LAND; case IN_AIR -> org.bukkit.entity.Boat.Status.IN_AIR; }; }
    @Override public org.bukkit.Material getBoatMaterial() { return org.bukkit.craftbukkit.v1_20_R1.util.CraftMagicNumbers.getMaterial(getHandle().asItem()); }
}
