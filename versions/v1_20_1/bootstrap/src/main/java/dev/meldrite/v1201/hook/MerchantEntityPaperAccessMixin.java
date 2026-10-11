package dev.meldrite.v1201.hook;

import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradeOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MerchantEntity.class)
public interface MerchantEntityPaperAccessMixin {
    @Accessor("offers") void meldrite$setOffers(TradeOfferList offers);
    @Invoker("fillRecipesFromPool") void meldrite$fillOffers(TradeOfferList offers, TradeOffers.Factory[] factories, int amount);
}
