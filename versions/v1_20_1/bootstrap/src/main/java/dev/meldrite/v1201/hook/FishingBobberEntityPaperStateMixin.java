package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.FishingBobberNativeAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingBobberEntity.class)
public abstract class FishingBobberEntityPaperStateMixin implements FishingBobberNativeAccess {
    @Accessor("hookedEntity")
    @Override public abstract Entity meldrite$getHookedEntity();
    @Accessor("hookedEntity")
    @Override public abstract void meldrite$setHookedEntity(Entity entity);
    @Invoker("pullHookedEntity")
    @Override public abstract void meldrite$pullHookedEntity(Entity entity);

    @Unique private int meldrite$minWaitTime = 100;
    @Unique private int meldrite$maxWaitTime = 600;
    @Unique private int meldrite$minLureTime;
    @Unique private int meldrite$maxLureTime;
    @Unique private float meldrite$minLureAngle;
    @Unique private float meldrite$maxLureAngle = 360.0F;
    @Unique private boolean meldrite$applyLure = true;
    @Unique private boolean meldrite$skyInfluenced = true;
    @Unique private boolean meldrite$rainInfluenced = true;
    @Unique private double meldrite$biteChance = -1.0D;
    @Unique private int meldrite$waitTime;

    @Override public int meldrite$getMinWaitTime() { return meldrite$minWaitTime; }
    @Override public void meldrite$setMinWaitTime(int value) { meldrite$minWaitTime = value; }
    @Override public int meldrite$getMaxWaitTime() { return meldrite$maxWaitTime; }
    @Override public void meldrite$setMaxWaitTime(int value) { meldrite$maxWaitTime = value; }
    @Override public int meldrite$getMinLureTime() { return meldrite$minLureTime; }
    @Override public void meldrite$setMinLureTime(int value) { meldrite$minLureTime = value; }
    @Override public int meldrite$getMaxLureTime() { return meldrite$maxLureTime; }
    @Override public void meldrite$setMaxLureTime(int value) { meldrite$maxLureTime = value; }
    @Override public float meldrite$getMinLureAngle() { return meldrite$minLureAngle; }
    @Override public void meldrite$setMinLureAngle(float value) { meldrite$minLureAngle = value; }
    @Override public float meldrite$getMaxLureAngle() { return meldrite$maxLureAngle; }
    @Override public void meldrite$setMaxLureAngle(float value) { meldrite$maxLureAngle = value; }
    @Override public boolean meldrite$getApplyLure() { return meldrite$applyLure; }
    @Override public void meldrite$setApplyLure(boolean value) { meldrite$applyLure = value; }
    @Override public boolean meldrite$isSkyInfluenced() { return meldrite$skyInfluenced; }
    @Override public void meldrite$setSkyInfluenced(boolean value) { meldrite$skyInfluenced = value; }
    @Override public boolean meldrite$isRainInfluenced() { return meldrite$rainInfluenced; }
    @Override public void meldrite$setRainInfluenced(boolean value) { meldrite$rainInfluenced = value; }
    @Override public double meldrite$getBiteChance() { return meldrite$biteChance; }
    @Override public void meldrite$setBiteChance(double value) { meldrite$biteChance = value; }
    @Override public int meldrite$getWaitTime() { return meldrite$waitTime; }
    @Override public void meldrite$setWaitTime(int value) { meldrite$waitTime = value; }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void meldrite$write(NbtCompound nbt, CallbackInfo ci) {
        if (meldrite$minWaitTime != 100) nbt.putInt("MeldriteMinWait", meldrite$minWaitTime);
        if (meldrite$maxWaitTime != 600) nbt.putInt("MeldriteMaxWait", meldrite$maxWaitTime);
        if (meldrite$minLureTime != 0) nbt.putInt("MeldriteMinLure", meldrite$minLureTime);
        if (meldrite$maxLureTime != 0) nbt.putInt("MeldriteMaxLure", meldrite$maxLureTime);
        if (meldrite$minLureAngle != 0) nbt.putFloat("MeldriteMinLureAngle", meldrite$minLureAngle);
        if (meldrite$maxLureAngle != 360.0F) nbt.putFloat("MeldriteMaxLureAngle", meldrite$maxLureAngle);
        if (!meldrite$applyLure) nbt.putBoolean("MeldriteApplyLure", false);
        if (!meldrite$skyInfluenced) nbt.putBoolean("MeldriteSkyInfluenced", false);
        if (!meldrite$rainInfluenced) nbt.putBoolean("MeldriteRainInfluenced", false);
        if (meldrite$biteChance != -1.0D) nbt.putDouble("MeldriteBiteChance", meldrite$biteChance);
        if (meldrite$waitTime != 0) nbt.putInt("MeldriteWaitTime", meldrite$waitTime);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void meldrite$read(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("MeldriteMinWait")) meldrite$minWaitTime = nbt.getInt("MeldriteMinWait");
        if (nbt.contains("MeldriteMaxWait")) meldrite$maxWaitTime = nbt.getInt("MeldriteMaxWait");
        if (nbt.contains("MeldriteMinLure")) meldrite$minLureTime = nbt.getInt("MeldriteMinLure");
        if (nbt.contains("MeldriteMaxLure")) meldrite$maxLureTime = nbt.getInt("MeldriteMaxLure");
        if (nbt.contains("MeldriteMinLureAngle")) meldrite$minLureAngle = nbt.getFloat("MeldriteMinLureAngle");
        if (nbt.contains("MeldriteMaxLureAngle")) meldrite$maxLureAngle = nbt.getFloat("MeldriteMaxLureAngle");
        if (nbt.contains("MeldriteApplyLure")) meldrite$applyLure = nbt.getBoolean("MeldriteApplyLure");
        if (nbt.contains("MeldriteSkyInfluenced")) meldrite$skyInfluenced = nbt.getBoolean("MeldriteSkyInfluenced");
        if (nbt.contains("MeldriteRainInfluenced")) meldrite$rainInfluenced = nbt.getBoolean("MeldriteRainInfluenced");
        if (nbt.contains("MeldriteBiteChance")) meldrite$biteChance = nbt.getDouble("MeldriteBiteChance");
        if (nbt.contains("MeldriteWaitTime")) meldrite$waitTime = nbt.getInt("MeldriteWaitTime");
    }
}
