package dev.meldrite.v1201.hook;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.meldrite.v1201.compat.NativeAgeableState;
import net.minecraft.entity.passive.TadpoleEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(TadpoleEntity.class)
public abstract class TadpoleEntityAgeMixin implements NativeAgeableState {
    @Unique private boolean meldrite$ageLocked;
    @Override public boolean meldrite$isAgeLocked() { return meldrite$ageLocked; }
    @Override public void meldrite$setAgeLocked(boolean lock) { meldrite$ageLocked = lock; }
    @WrapOperation(method = {"tickMovement", "increaseAge"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/TadpoleEntity;setTadpoleAge(I)V"))
    private void meldrite$preventGrowth(TadpoleEntity entity, int age, Operation<Void> original) { if (!meldrite$ageLocked) original.call(entity, age); }
    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL")) private void meldrite$write(NbtCompound nbt, CallbackInfo ci) { if (meldrite$ageLocked) nbt.putBoolean("AgeLocked", true); }
    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL")) private void meldrite$read(NbtCompound nbt, CallbackInfo ci) { meldrite$ageLocked = nbt.getBoolean("AgeLocked"); }
    @Inject(method = "copyDataToStack", at = @At("TAIL")) private void meldrite$writeBucket(net.minecraft.item.ItemStack stack, CallbackInfo ci) { if (meldrite$ageLocked) stack.getOrCreateNbt().putBoolean("AgeLocked", true); }
    @Inject(method = "copyDataFromNbt", at = @At("TAIL")) private void meldrite$readBucket(NbtCompound nbt, CallbackInfo ci) { meldrite$ageLocked = nbt.getBoolean("AgeLocked"); }
}
