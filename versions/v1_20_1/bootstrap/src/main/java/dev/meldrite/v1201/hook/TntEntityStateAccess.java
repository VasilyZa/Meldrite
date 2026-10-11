package dev.meldrite.v1201.hook;

import net.minecraft.entity.TntEntity;
public interface TntEntityStateAccess extends dev.meldrite.v1201.compat.TntNativeAccess {
    net.minecraft.entity.LivingEntity meldrite$getSource();
    void meldrite$setSource(net.minecraft.entity.LivingEntity value);
    float meldrite$getYield();
    void meldrite$setYield(float value);
    boolean meldrite$isIncendiary();
    void meldrite$setIncendiary(boolean value);
}
