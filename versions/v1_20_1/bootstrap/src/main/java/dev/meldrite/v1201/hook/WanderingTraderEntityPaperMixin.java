package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeTraderState;
import net.minecraft.entity.passive.WanderingTraderEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WanderingTraderEntity.class)
public abstract class WanderingTraderEntityPaperMixin implements NativeTraderState {
    @Unique private boolean meldrite$drinkPotion = true;
    @Unique private boolean meldrite$drinkMilk = true;
    @Override public boolean meldrite$canDrinkPotion() { return meldrite$drinkPotion; }
    @Override public void meldrite$canDrinkPotion(boolean value) { meldrite$drinkPotion = value; }
    @Override public boolean meldrite$canDrinkMilk() { return meldrite$drinkMilk; }
    @Override public void meldrite$canDrinkMilk(boolean value) { meldrite$drinkMilk = value; }
    @Inject(method = "method_18068", at = @At("HEAD"), cancellable = true)
    private void meldrite$disablePotion(WanderingTraderEntity trader, CallbackInfoReturnable<Boolean> cir) { if (!meldrite$drinkPotion) cir.setReturnValue(false); }
    @Inject(method = "method_18067", at = @At("HEAD"), cancellable = true)
    private void meldrite$disableMilk(WanderingTraderEntity trader, CallbackInfoReturnable<Boolean> cir) { if (!meldrite$drinkMilk) cir.setReturnValue(false); }
}
