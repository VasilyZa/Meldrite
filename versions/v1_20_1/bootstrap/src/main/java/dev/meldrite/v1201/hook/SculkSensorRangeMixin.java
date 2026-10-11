package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeSensorRange;
import net.minecraft.block.entity.SculkSensorBlockEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SculkSensorBlockEntity.class)
public abstract class SculkSensorRangeMixin implements NativeSensorRange {
    @Unique private int meldrite$range = -1;
    @Override public int meldrite$getRange() { return meldrite$range; }
    @Override public void meldrite$setRange(int value) { meldrite$range = value; }
    @Inject(method = "writeNbt", at = @At("TAIL"))
    private void meldrite$write(NbtCompound nbt, CallbackInfo callback) {
        if (meldrite$range > 0) nbt.putInt("Paper.ListenerRange", meldrite$range);
    }
    @Inject(method = "readNbt", at = @At("TAIL"))
    private void meldrite$read(NbtCompound nbt, CallbackInfo callback) {
        meldrite$range = nbt.contains("Paper.ListenerRange") ? nbt.getInt("Paper.ListenerRange") : -1;
    }
}
