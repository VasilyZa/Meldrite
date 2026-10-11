package org.bukkit.craftbukkit.v1_20_R1.inventory;

import dev.meldrite.v1201.compat.NativeMerchantAccess;
import dev.meldrite.v1201.compat.NativeTradeOfferState;
import dev.meldrite.v1201.hook.MerchantInventoryAccess;
import net.minecraft.village.Merchant;
import net.minecraft.village.MerchantInventory;
import org.bukkit.inventory.MerchantRecipe;

public class CraftInventoryMerchant extends CraftInventory implements org.bukkit.inventory.MerchantInventory {
    private final Merchant merchant;
    public CraftInventoryMerchant(Merchant merchant, MerchantInventory inventory) {
        super(inventory, null, org.bukkit.event.inventory.InventoryType.MERCHANT);
        this.merchant = merchant;
    }
    @Override public MerchantInventory getInventory() { return (MerchantInventory) inventory; }
    @Override public int getSelectedRecipeIndex() { return ((MerchantInventoryAccess) inventory).meldrite$getOfferIndex(); }
    @Override public MerchantRecipe getSelectedRecipe() {
        var offer = getInventory().getTradeOffer();
        return offer == null ? null : ((NativeTradeOfferState) offer).asBukkit();
    }
    @Override public org.bukkit.inventory.Merchant getMerchant() { return ((NativeMerchantAccess) merchant).getCraftMerchant(); }
}
