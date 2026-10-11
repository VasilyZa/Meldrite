package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeSunburnState;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ZombieEntity.class)
public abstract class ZombieEntitySunburnMixin implements NativeSunburnState {
    @Unique private boolean meldrite$burnInDay = true;
    @Unique private boolean meldrite$burnInDayOverridden;
    @Override public boolean meldrite$hasSunburnOverride() { return meldrite$burnInDayOverridden; }
    @Override public boolean meldrite$shouldBurnInDay() { return ((ZombieEntityPaperAccessMixin) this).meldrite$burnsInDaylight(); }
    @Override public void meldrite$setShouldBurnInDay(boolean value) { meldrite$burnInDay = value; meldrite$burnInDayOverridden = true; }
    @Inject(method = "burnsInDaylight", at = @At("HEAD"), cancellable = true)
    private void meldrite$daylightRule(CallbackInfoReturnable<Boolean> callback) { if (meldrite$burnInDayOverridden) callback.setReturnValue(meldrite$burnInDay); }
    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void meldrite$writeBurnRule(NbtCompound nbt, CallbackInfo callback) { if (meldrite$burnInDayOverridden) nbt.putBoolean("Paper.ShouldBurnInDay", meldrite$burnInDay); }
    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void meldrite$readBurnRule(NbtCompound nbt, CallbackInfo callback) { meldrite$burnInDayOverridden = nbt.contains("Paper.ShouldBurnInDay"); meldrite$burnInDay = !meldrite$burnInDayOverridden || nbt.getBoolean("Paper.ShouldBurnInDay"); }
}
