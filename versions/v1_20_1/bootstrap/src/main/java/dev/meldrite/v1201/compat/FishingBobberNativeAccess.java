package dev.meldrite.v1201.compat;

import net.minecraft.entity.Entity;

/** Paper fishing-hook state carried by a native FishingBobberEntity. */
public interface FishingBobberNativeAccess {
    Entity meldrite$getHookedEntity();
    void meldrite$setHookedEntity(Entity entity);
    void meldrite$pullHookedEntity(Entity entity);
    int meldrite$getMinWaitTime();
    void meldrite$setMinWaitTime(int value);
    int meldrite$getMaxWaitTime();
    void meldrite$setMaxWaitTime(int value);
    int meldrite$getMinLureTime();
    void meldrite$setMinLureTime(int value);
    int meldrite$getMaxLureTime();
    void meldrite$setMaxLureTime(int value);
    float meldrite$getMinLureAngle();
    void meldrite$setMinLureAngle(float value);
    float meldrite$getMaxLureAngle();
    void meldrite$setMaxLureAngle(float value);
    boolean meldrite$getApplyLure();
    void meldrite$setApplyLure(boolean value);
    boolean meldrite$isSkyInfluenced();
    void meldrite$setSkyInfluenced(boolean value);
    boolean meldrite$isRainInfluenced();
    void meldrite$setRainInfluenced(boolean value);
    double meldrite$getBiteChance();
    void meldrite$setBiteChance(double value);
    int meldrite$getWaitTime();
    void meldrite$setWaitTime(int value);
}
