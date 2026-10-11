package dev.meldrite.v1201.compat;
public interface BoatNativeAccess {
    float meldrite$getVelocityDecay(); void meldrite$setVelocityDecay(float value);
    net.minecraft.entity.vehicle.BoatEntity.Location meldrite$getLocation();
    double meldrite$getMaxSpeed(); void meldrite$setMaxSpeed(double value);
    double meldrite$getOccupiedDeceleration(); void meldrite$setOccupiedDeceleration(double value);
    double meldrite$getUnoccupiedDeceleration(); void meldrite$setUnoccupiedDeceleration(double value);
    boolean meldrite$getLandBoats(); void meldrite$setLandBoats(boolean value);
}
