package dev.meldrite.v1201.hook;

import net.minecraft.entity.FallingBlockEntity;
public interface FallingBlockEntityStateAccess extends dev.meldrite.v1201.compat.FallingBlockNativeAccess {
    boolean meldrite$getCancelDrop();
    void meldrite$setCancelDrop(boolean value);
    boolean meldrite$getHurtEntities();
    void meldrite$setHurtEntities(boolean value);
    int meldrite$getMaxDamage();
    void meldrite$setMaxDamage(int value);
    float meldrite$getDamagePerBlock();
    void meldrite$setDamagePerBlock(float value);
    net.minecraft.block.BlockState meldrite$getBlock();
    boolean meldrite$doesAutoExpire();
    void meldrite$setAutoExpire(boolean value);
}
