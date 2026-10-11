package dev.meldrite.v1201.hook;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.meldrite.v1201.compat.NativeTradeOfferState;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.village.TradeOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(VillagerEntity.class)
public abstract class VillagerTradeDiscountMixin {
    @WrapOperation(method = "prepareOffersFor", at = @At(value = "INVOKE", target = "Lnet/minecraft/village/TradeOffer;increaseSpecialPrice(I)V"))
    private void meldrite$discount(TradeOffer offer, int discount, Operation<Void> original) {
        if (!((NativeTradeOfferState) offer).meldrite$ignoreDiscounts()) original.call(offer, discount);
    }
}
