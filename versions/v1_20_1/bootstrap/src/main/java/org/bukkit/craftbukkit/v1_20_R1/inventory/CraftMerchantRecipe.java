package org.bukkit.craftbukkit.v1_20_R1.inventory;

import dev.meldrite.v1201.compat.NativeTradeOfferState;
import dev.meldrite.v1201.hook.TradeOfferAccess;
import net.minecraft.village.TradeOffer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

public class CraftMerchantRecipe extends MerchantRecipe {
    private final TradeOffer handle;
    public CraftMerchantRecipe(TradeOffer offer) {
        super(CraftItemStack.asBukkitCopy(offer.getSellItem()), 0);
        handle = offer;
        addIngredient(CraftItemStack.asBukkitCopy(offer.getOriginalFirstBuyItem()));
        addIngredient(CraftItemStack.asBukkitCopy(offer.getSecondBuyItem()));
        state().meldrite$setBukkitRecipe(this);
    }
    public CraftMerchantRecipe(ItemStack result, int uses, int maxUses, boolean reward, int experience, float multiplier) {
        this(result, uses, maxUses, reward, experience, multiplier, 0, 0, false);
    }
    public CraftMerchantRecipe(ItemStack result, int uses, int maxUses, boolean reward, int experience, float multiplier, boolean ignoreDiscounts) {
        this(result, uses, maxUses, reward, experience, multiplier, 0, 0, ignoreDiscounts);
    }
    public CraftMerchantRecipe(ItemStack result, int uses, int maxUses, boolean reward, int experience, float multiplier, int demand, int specialPrice) {
        this(result, uses, maxUses, reward, experience, multiplier, demand, specialPrice, false);
    }
    public CraftMerchantRecipe(ItemStack result, int uses, int maxUses, boolean reward, int experience, float multiplier, int demand, int specialPrice, boolean ignoreDiscounts) {
        super(result, uses, maxUses, reward, experience, multiplier, demand, specialPrice, ignoreDiscounts);
        handle = new TradeOffer(net.minecraft.item.ItemStack.EMPTY, net.minecraft.item.ItemStack.EMPTY,
                CraftItemStack.asNMSCopy(result), uses, maxUses, experience, multiplier, demand);
        setSpecialPrice(specialPrice);
        setExperienceReward(reward);
        setIgnoreDiscounts(ignoreDiscounts);
        state().meldrite$setBukkitRecipe(this);
    }
    private TradeOfferAccess access() { return (TradeOfferAccess) handle; }
    private NativeTradeOfferState state() { return (NativeTradeOfferState) handle; }
    @Override public int getUses() { return handle.getUses(); }
    @Override public void setUses(int uses) { access().meldrite$setUses(uses); }
    @Override public int getMaxUses() { return handle.getMaxUses(); }
    @Override public void setMaxUses(int uses) { access().meldrite$setMaxUses(uses); }
    @Override public boolean hasExperienceReward() { return handle.shouldRewardPlayerExperience(); }
    @Override public void setExperienceReward(boolean reward) { access().meldrite$setRewardingPlayerExperience(reward); }
    @Override public int getVillagerExperience() { return handle.getMerchantExperience(); }
    @Override public void setVillagerExperience(int experience) { access().meldrite$setMerchantExperience(experience); }
    @Override public float getPriceMultiplier() { return handle.getPriceMultiplier(); }
    @Override public void setPriceMultiplier(float multiplier) { access().meldrite$setPriceMultiplier(multiplier); }
    @Override public int getSpecialPrice() { return handle.getSpecialPrice(); }
    @Override public void setSpecialPrice(int price) { handle.setSpecialPrice(price); }
    @Override public int getDemand() { return handle.getDemandBonus(); }
    @Override public void setDemand(int demand) { access().meldrite$setDemandBonus(demand); }
    @Override public boolean shouldIgnoreDiscounts() { return state().meldrite$ignoreDiscounts(); }
    @Override public void setIgnoreDiscounts(boolean ignore) { state().meldrite$ignoreDiscounts(ignore); }
    public TradeOffer toMinecraft() {
        var ingredients = getIngredients();
        if (ingredients.isEmpty()) throw new IllegalStateException("No offered ingredients");
        access().meldrite$setFirstBuyItem(CraftItemStack.asNMSCopy(ingredients.get(0)));
        access().meldrite$setSecondBuyItem(ingredients.size() > 1 ? CraftItemStack.asNMSCopy(ingredients.get(1)) : net.minecraft.item.ItemStack.EMPTY);
        return handle;
    }
    public static CraftMerchantRecipe fromBukkit(MerchantRecipe recipe) {
        if (recipe instanceof CraftMerchantRecipe craft) return craft;
        var craft = new CraftMerchantRecipe(recipe.getResult(), recipe.getUses(), recipe.getMaxUses(), recipe.hasExperienceReward(),
                recipe.getVillagerExperience(), recipe.getPriceMultiplier(), recipe.getDemand(), recipe.getSpecialPrice(), recipe.shouldIgnoreDiscounts());
        craft.setIngredients(recipe.getIngredients());
        return craft;
    }
}
