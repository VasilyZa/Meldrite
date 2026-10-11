package dev.meldrite.v1201.hook;

import net.minecraft.item.ItemStack;
import net.minecraft.village.TradeOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TradeOffer.class)
public interface TradeOfferAccess {
    @Mutable @Accessor("firstBuyItem") void meldrite$setFirstBuyItem(ItemStack item);
    @Mutable @Accessor("secondBuyItem") void meldrite$setSecondBuyItem(ItemStack item);
    @Accessor("uses") void meldrite$setUses(int uses);
    @Mutable @Accessor("maxUses") void meldrite$setMaxUses(int uses);
    @Accessor("rewardingPlayerExperience") void meldrite$setRewardingPlayerExperience(boolean reward);
    @Accessor("demandBonus") void meldrite$setDemandBonus(int demand);
    @Accessor("priceMultiplier") void meldrite$setPriceMultiplier(float multiplier);
    @Accessor("merchantExperience") void meldrite$setMerchantExperience(int experience);
}
