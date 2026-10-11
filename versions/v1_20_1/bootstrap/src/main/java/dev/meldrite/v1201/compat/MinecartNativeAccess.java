package dev.meldrite.v1201.compat;
public interface MinecartNativeAccess {
    boolean meldrite$isSlowWhenEmpty(); void meldrite$setSlowWhenEmpty(boolean value);
    boolean meldrite$isOnRail();
    double meldrite$getMaxSpeed(); void meldrite$setMaxSpeed(double value);
    org.bukkit.util.Vector meldrite$getFlyingVelocityMod(); void meldrite$setFlyingVelocityMod(org.bukkit.util.Vector value);
    org.bukkit.util.Vector meldrite$getDerailedVelocityMod(); void meldrite$setDerailedVelocityMod(org.bukkit.util.Vector value);
}
