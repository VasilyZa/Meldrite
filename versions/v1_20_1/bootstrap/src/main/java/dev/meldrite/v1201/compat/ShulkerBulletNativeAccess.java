package dev.meldrite.v1201.compat;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Direction;

public interface ShulkerBulletNativeAccess {
    Entity meldrite$getTarget();
    void meldrite$setTarget(Entity value);
    Direction meldrite$getDirection();
    void meldrite$setDirection(Direction value);
    int meldrite$getFlightSteps();
    void meldrite$setFlightSteps(int value);
    double meldrite$getTargetX();
    double meldrite$getTargetY();
    double meldrite$getTargetZ();
    void meldrite$setTargetX(double value);
    void meldrite$setTargetY(double value);
    void meldrite$setTargetZ(double value);
}
