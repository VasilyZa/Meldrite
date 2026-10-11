package dev.meldrite.v1201.hook;
import net.minecraft.entity.vehicle.TntMinecartEntity;
public interface TntMinecartEntityStateAccess extends dev.meldrite.v1201.compat.TntMinecartNativeAccess {
    int meldrite$getFuseTicks();
    void meldrite$setFuseTicks(int value);
}
