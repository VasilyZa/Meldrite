package dev.meldrite.v1201.hook;

import net.minecraft.entity.vehicle.HopperMinecartEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HopperMinecartEntity.class)
public abstract class HopperMinecartCooldownMixin implements dev.meldrite.v1201.compat.HopperMinecartCooldownAccess {
    @Unique private int meldrite$pickupCooldown;
    @Override public int meldrite$getPickupCooldown() { return meldrite$pickupCooldown; }
    @Override public void meldrite$setPickupCooldown(int value) { meldrite$pickupCooldown = value; }
    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void meldrite$write(NbtCompound nbt, CallbackInfo ci) { if (meldrite$pickupCooldown != 0) nbt.putInt("MeldritePickupCooldown", meldrite$pickupCooldown); }
    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void meldrite$read(NbtCompound nbt, CallbackInfo ci) { if (nbt.contains("MeldritePickupCooldown")) meldrite$pickupCooldown = nbt.getInt("MeldritePickupCooldown"); }
}
