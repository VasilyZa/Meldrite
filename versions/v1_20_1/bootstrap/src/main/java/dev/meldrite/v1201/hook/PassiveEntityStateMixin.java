package dev.meldrite.v1201.hook;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.meldrite.v1201.compat.NativeAgeableState;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PassiveEntity.class)
public abstract class PassiveEntityStateMixin implements NativeAgeableState {
    @Unique private boolean meldrite$ageLocked;
    @Override public boolean meldrite$isAgeLocked() { return meldrite$ageLocked; }
    @Override public void meldrite$setAgeLocked(boolean lock) { meldrite$ageLocked = lock; }

    @Inject(method = "growUp(IZ)V", at = @At("HEAD"), cancellable = true)
    private void meldrite$preventForcedGrowth(int age, boolean overGrow, CallbackInfo ci) {
        if (meldrite$ageLocked) ci.cancel();
    }

    @WrapOperation(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/PassiveEntity;setBreedingAge(I)V"))
    private void meldrite$preventNaturalGrowth(PassiveEntity entity, int age, Operation<Void> original) {
        if (!meldrite$ageLocked) original.call(entity, age);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void meldrite$writeAgeLock(NbtCompound nbt, CallbackInfo ci) {
        if (meldrite$ageLocked) nbt.putBoolean("AgeLocked", true);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void meldrite$readAgeLock(NbtCompound nbt, CallbackInfo ci) {
        meldrite$ageLocked = nbt.getBoolean("AgeLocked");
    }
}
