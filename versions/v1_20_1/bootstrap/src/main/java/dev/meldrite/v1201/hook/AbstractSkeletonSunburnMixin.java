package dev.meldrite.v1201.hook;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.meldrite.v1201.compat.NativeSunburnState;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSkeletonEntity.class)
public abstract class AbstractSkeletonSunburnMixin implements NativeSunburnState {
    @Unique private boolean meldrite$burnInDay = true;
    @Unique private boolean meldrite$burnInDayOverridden;
    @Override public boolean meldrite$hasSunburnOverride() { return meldrite$burnInDayOverridden; }
    @Override public boolean meldrite$shouldBurnInDay() { return meldrite$burnInDay; }
    @Override public void meldrite$setShouldBurnInDay(boolean value) { meldrite$burnInDay = value; meldrite$burnInDayOverridden = true; }
    @WrapOperation(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/AbstractSkeletonEntity;isAffectedByDaylight()Z"))
    private boolean meldrite$daylightRule(AbstractSkeletonEntity entity, Operation<Boolean> original) { return meldrite$burnInDay && original.call(entity); }
    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void meldrite$readBurnRule(NbtCompound nbt, CallbackInfo callback) { meldrite$burnInDayOverridden = nbt.contains("Paper.ShouldBurnInDay"); meldrite$burnInDay = !meldrite$burnInDayOverridden || nbt.getBoolean("Paper.ShouldBurnInDay"); }
}
