package dev.meldrite.v1201.hook;
import dev.meldrite.v1201.compat.NativeAllayState;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import net.minecraft.entity.passive.AllayEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(AllayEntity.class)
public abstract class AllayEntityDancingMixin implements NativeAllayState {
    @Unique private boolean meldrite$forceDancing;
    @Override public void meldrite$forceDancing(boolean value) { meldrite$forceDancing = value; }
    @Inject(method = "shouldStopDancing", at = @At("HEAD"), cancellable = true)
    private void meldrite$continueDancing(CallbackInfoReturnable<Boolean> cir) { if (meldrite$forceDancing) cir.setReturnValue(false); }
    @Inject(method = "duplicate", at = @At("HEAD"), cancellable = true)
    private void meldrite$duplicate(CallbackInfo ci) { dev.meldrite.v1201.compat.NativeAllayBehavior.duplicate((AllayEntity) (Object) this); ci.cancel(); }
    @WrapOperation(method = "interactMob", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/AllayEntity;duplicate()V"))
    private void meldrite$interactionDuplicate(AllayEntity allay, Operation<Void> original, @Cancellable CallbackInfoReturnable<net.minecraft.util.ActionResult> cir) {
        if (dev.meldrite.v1201.compat.NativeAllayBehavior.duplicate(allay) == null) cir.setReturnValue(net.minecraft.util.ActionResult.SUCCESS);
    }
}
