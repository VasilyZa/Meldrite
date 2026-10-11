package dev.meldrite.v1201.hook;

import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BoatEntity.class)
public abstract class BoatEntityPaperStateMixin implements dev.meldrite.v1201.compat.BoatNativeAccess {
    @Unique private double meldrite$maxSpeed = 0.4D;
    @Unique private double meldrite$occupiedDeceleration = 0.2D;
    @Unique private double meldrite$unoccupiedDeceleration = -1.0D;
    @Unique private boolean meldrite$landBoats;

    @Accessor("velocityDecay") public abstract float meldrite$getVelocityDecay();
    @Accessor("velocityDecay") public abstract void meldrite$setVelocityDecay(float value);
    @Override @Accessor("location") public abstract BoatEntity.Location meldrite$getLocation();

    @Override public double meldrite$getMaxSpeed() { return meldrite$maxSpeed; }
    @Override public void meldrite$setMaxSpeed(double value) { meldrite$maxSpeed = value; }
    @Override public double meldrite$getOccupiedDeceleration() { return meldrite$occupiedDeceleration; }
    @Override public void meldrite$setOccupiedDeceleration(double value) { meldrite$occupiedDeceleration = value; }
    @Override public double meldrite$getUnoccupiedDeceleration() { return meldrite$unoccupiedDeceleration; }
    @Override public void meldrite$setUnoccupiedDeceleration(double value) { meldrite$unoccupiedDeceleration = value; }
    @Override public boolean meldrite$getLandBoats() { return meldrite$landBoats; }
    @Override public void meldrite$setLandBoats(boolean value) { meldrite$landBoats = value; }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void meldrite$write(NbtCompound nbt, CallbackInfo ci) {
        if (meldrite$maxSpeed != 0.4D) nbt.putDouble("MeldriteMaxSpeed", meldrite$maxSpeed);
        if (meldrite$occupiedDeceleration != 0.2D) nbt.putDouble("MeldriteOccupiedDeceleration", meldrite$occupiedDeceleration);
        if (meldrite$unoccupiedDeceleration != -1.0D) nbt.putDouble("MeldriteUnoccupiedDeceleration", meldrite$unoccupiedDeceleration);
        if (meldrite$landBoats) nbt.putBoolean("MeldriteLandBoats", true);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void meldrite$read(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("MeldriteMaxSpeed")) meldrite$maxSpeed = nbt.getDouble("MeldriteMaxSpeed");
        if (nbt.contains("MeldriteOccupiedDeceleration")) meldrite$occupiedDeceleration = nbt.getDouble("MeldriteOccupiedDeceleration");
        if (nbt.contains("MeldriteUnoccupiedDeceleration")) meldrite$unoccupiedDeceleration = nbt.getDouble("MeldriteUnoccupiedDeceleration");
        meldrite$landBoats = nbt.getBoolean("MeldriteLandBoats");
    }
}
