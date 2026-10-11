package dev.meldrite.v1201.hook;

import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.nbt.NbtCompound;
import org.bukkit.util.Vector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractMinecartEntity.class)
public abstract class AbstractMinecartEntityPaperStateMixin implements dev.meldrite.v1201.compat.MinecartNativeAccess {
    @Unique private double meldrite$maxSpeed = 0.4D;
    @Unique private boolean meldrite$maxSpeedOverridden;
    @Unique private Vector meldrite$flyingVelocityMod = new Vector(0.949999988079071D, 0.949999988079071D, 0.949999988079071D);
    @Unique private Vector meldrite$derailedVelocityMod = new Vector(0.5D, 0.5D, 0.5D);

    @Override @Accessor("yawFlipped") public abstract boolean meldrite$isSlowWhenEmpty();
    @Override @Accessor("yawFlipped") public abstract void meldrite$setSlowWhenEmpty(boolean value);
    @Override @Accessor("onRail") public abstract boolean meldrite$isOnRail();

    @Override public double meldrite$getMaxSpeed() { return meldrite$maxSpeed; }
    @Override public void meldrite$setMaxSpeed(double value) { meldrite$maxSpeed = value; meldrite$maxSpeedOverridden = true; }
    @Override public Vector meldrite$getFlyingVelocityMod() { return meldrite$flyingVelocityMod.clone(); }
    @Override public void meldrite$setFlyingVelocityMod(Vector value) { meldrite$flyingVelocityMod = value.clone(); }
    @Override public Vector meldrite$getDerailedVelocityMod() { return meldrite$derailedVelocityMod.clone(); }
    @Override public void meldrite$setDerailedVelocityMod(Vector value) { meldrite$derailedVelocityMod = value.clone(); }

    @Inject(method = "getMaxSpeed", at = @At("RETURN"), cancellable = true)
    private void meldrite$maxSpeed(CallbackInfoReturnable<Double> cir) { if (meldrite$maxSpeedOverridden) cir.setReturnValue(meldrite$maxSpeed); }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void meldrite$write(NbtCompound nbt, CallbackInfo ci) {
        if (meldrite$maxSpeedOverridden) nbt.putDouble("MeldriteMaxSpeed", meldrite$maxSpeed);
        if (!meldrite$flyingVelocityMod.equals(new Vector(0.949999988079071D, 0.949999988079071D, 0.949999988079071D))) {
            nbt.putDouble("MeldriteFlyingVelocityX", meldrite$flyingVelocityMod.getX());
            nbt.putDouble("MeldriteFlyingVelocityY", meldrite$flyingVelocityMod.getY());
            nbt.putDouble("MeldriteFlyingVelocityZ", meldrite$flyingVelocityMod.getZ());
        }
        if (!meldrite$derailedVelocityMod.equals(new Vector(0.5D, 0.5D, 0.5D))) {
            nbt.putDouble("MeldriteDerailedVelocityX", meldrite$derailedVelocityMod.getX());
            nbt.putDouble("MeldriteDerailedVelocityY", meldrite$derailedVelocityMod.getY());
            nbt.putDouble("MeldriteDerailedVelocityZ", meldrite$derailedVelocityMod.getZ());
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void meldrite$read(NbtCompound nbt, CallbackInfo ci) {
        meldrite$maxSpeedOverridden = nbt.contains("MeldriteMaxSpeed");
        if (meldrite$maxSpeedOverridden) meldrite$maxSpeed = nbt.getDouble("MeldriteMaxSpeed");
        if (nbt.contains("MeldriteFlyingVelocityX")) meldrite$flyingVelocityMod = new Vector(nbt.getDouble("MeldriteFlyingVelocityX"), nbt.getDouble("MeldriteFlyingVelocityY"), nbt.getDouble("MeldriteFlyingVelocityZ"));
        if (nbt.contains("MeldriteDerailedVelocityX")) meldrite$derailedVelocityMod = new Vector(nbt.getDouble("MeldriteDerailedVelocityX"), nbt.getDouble("MeldriteDerailedVelocityY"), nbt.getDouble("MeldriteDerailedVelocityZ"));
    }
}
