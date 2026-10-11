package dev.meldrite.v1201.hook;

import net.minecraft.entity.vehicle.BoatEntity;
public interface BoatEntityStateAccess extends dev.meldrite.v1201.compat.BoatNativeAccess {
    BoatEntity.Location meldrite$getLocation();
    double meldrite$getMaxSpeed();
    void meldrite$setMaxSpeed(double value);
    double meldrite$getOccupiedDeceleration();
    void meldrite$setOccupiedDeceleration(double value);
    double meldrite$getUnoccupiedDeceleration();
    void meldrite$setUnoccupiedDeceleration(double value);
    boolean meldrite$getLandBoats();
    void meldrite$setLandBoats(boolean value);
}
