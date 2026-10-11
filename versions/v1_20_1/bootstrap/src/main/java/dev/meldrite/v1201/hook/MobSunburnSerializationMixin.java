package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeSunburnState;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public abstract class MobSunburnSerializationMixin {
    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void meldrite$writeSkeletonBurnRule(NbtCompound nbt, CallbackInfo callback) { if ((Object) this instanceof NativeSunburnState state && state.meldrite$hasSunburnOverride()) nbt.putBoolean("Paper.ShouldBurnInDay", state.meldrite$shouldBurnInDay()); }
}
