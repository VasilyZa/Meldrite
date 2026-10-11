package org.bukkit.craftbukkit.v1_20_R1.inventory;

import dev.meldrite.v1201.compat.NativeEntityExtensions;
import dev.meldrite.v1201.compat.NativeTradeOfferState;
import java.util.List;
import net.minecraft.village.Merchant;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.MerchantRecipe;

public class CraftMerchant implements org.bukkit.inventory.Merchant {
    protected final Merchant merchant;
    public CraftMerchant(Merchant merchant) { this.merchant = java.util.Objects.requireNonNull(merchant, "merchant"); }
    public Merchant getMerchant() { return merchant; }
    @Override public List<MerchantRecipe> getRecipes() { return merchant.getOffers().stream().<MerchantRecipe>map(offer -> ((NativeTradeOfferState) offer).asBukkit()).toList(); }
    @Override public void setRecipes(List<MerchantRecipe> recipes) {
        var offers = recipes.stream().map(recipe -> CraftMerchantRecipe.fromBukkit(recipe).toMinecraft()).toList();
        merchant.getOffers().clear();
        merchant.getOffers().addAll(offers);
    }
    @Override public MerchantRecipe getRecipe(int index) { return ((NativeTradeOfferState) merchant.getOffers().get(index)).asBukkit(); }
    @Override public void setRecipe(int index, MerchantRecipe recipe) { merchant.getOffers().set(index, CraftMerchantRecipe.fromBukkit(recipe).toMinecraft()); }
    @Override public int getRecipeCount() { return merchant.getOffers().size(); }
    @Override public boolean isTrading() { return merchant.getCustomer() != null; }
    @Override public HumanEntity getTrader() { return merchant.getCustomer() == null ? null : (HumanEntity) NativeEntityExtensions.bukkit(merchant.getCustomer()); }
    @Override public int hashCode() { return merchant.hashCode(); }
    @Override public boolean equals(Object other) { return other instanceof CraftMerchant craft && merchant.equals(craft.merchant); }
}
