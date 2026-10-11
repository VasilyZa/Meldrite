package dev.meldrite.v1201.hook;

import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
public interface FurnaceMinecartEntityStateAccess extends dev.meldrite.v1201.compat.FurnaceMinecartNativeAccess {
    int meldrite$getFuel();
    void meldrite$setFuel(int value);
}
