package org.bukkit.craftbukkit.v1_20_R1.inventory;

import dev.meldrite.v1201.compat.NativeMerchantAccess;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.village.Merchant;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;

public class CraftMerchantCustom extends CraftMerchant {
    public CraftMerchantCustom(String title) { this(new MinecraftMerchant(org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage.fromString(java.util.Objects.requireNonNull(title))[0])); }
    public CraftMerchantCustom(net.kyori.adventure.text.Component title) {
        this(new MinecraftMerchant(Text.Serializer.fromJson(net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson().serialize(java.util.Objects.requireNonNull(title)))));
    }
    private CraftMerchantCustom(MinecraftMerchant merchant) { super(merchant); merchant.craftMerchant = this; }
    @Override public MinecraftMerchant getMerchant() { return (MinecraftMerchant) merchant; }

    public static class MinecraftMerchant implements Merchant, NativeMerchantAccess {
        private final Text title;
        private final TradeOfferList offers = new TradeOfferList();
        private PlayerEntity customer;
        private int experience;
        private CraftMerchantCustom craftMerchant;
        public MinecraftMerchant(String title) { this(org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage.fromString(title)[0]); }
        public MinecraftMerchant(net.kyori.adventure.text.Component title) { this(Text.Serializer.fromJson(net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson().serialize(title))); }
        private MinecraftMerchant(Text title) { this.title = java.util.Objects.requireNonNull(title, "title"); }
        public Text getTitle() { return title; }
        public Text getScoreboardDisplayName() { return title; }
        @Override public CraftMerchantCustom getCraftMerchant() { return craftMerchant; }
        @Override public void setCustomer(PlayerEntity customer) { this.customer = customer; }
        @Override public PlayerEntity getCustomer() { return customer; }
        @Override public TradeOfferList getOffers() { return offers; }
        @Override public void setOffersFromServer(TradeOfferList offers) { var copy = java.util.List.copyOf(offers); this.offers.clear(); this.offers.addAll(copy); }
        @Override public void trade(TradeOffer offer) {
            offer.use();
            if (customer != null && !customer.getWorld().isClient && offer.shouldRewardPlayerExperience()) {
                customer.getWorld().spawnEntity(new ExperienceOrbEntity(customer.getWorld(), customer.getX(), customer.getY(), customer.getZ(), offer.getMerchantExperience()));
            }
        }
        // Custom merchants have no entity animation or ambient trade sound to update.
        @Override public void onSellingItem(ItemStack item) { }
        @Override public int getExperience() { return experience; }
        @Override public void setExperienceFromServer(int experience) { this.experience = experience; }
        @Override public boolean isLeveledMerchant() { return false; }
        @Override public SoundEvent getYesSound() { return SoundEvents.ENTITY_VILLAGER_YES; }
        @Override public boolean isClient() { return false; }
    }
}
