package dev.meldrite.v1201.compat;

import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftMerchantRecipe;

public interface NativeTradeOfferState {
    boolean meldrite$ignoreDiscounts();
    void meldrite$ignoreDiscounts(boolean ignore);
    CraftMerchantRecipe asBukkit();
    void meldrite$setBukkitRecipe(CraftMerchantRecipe recipe);
}
