package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeTradeOfferState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.village.TradeOffer;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftMerchantRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TradeOffer.class)
public abstract class TradeOfferBukkitMixin implements NativeTradeOfferState {
    @Unique private CraftMerchantRecipe meldrite$bukkitRecipe;
    @Unique private boolean meldrite$ignoreDiscounts;
    @Override public boolean meldrite$ignoreDiscounts() { return meldrite$ignoreDiscounts; }
    @Override public void meldrite$ignoreDiscounts(boolean ignore) { meldrite$ignoreDiscounts = ignore; }
    @Override public void meldrite$setBukkitRecipe(CraftMerchantRecipe recipe) { meldrite$bukkitRecipe = recipe; }
    @Override public CraftMerchantRecipe asBukkit() {
        if (meldrite$bukkitRecipe == null) meldrite$bukkitRecipe = new CraftMerchantRecipe((TradeOffer) (Object) this);
        return meldrite$bukkitRecipe;
    }
    @Inject(method = "<init>(Lnet/minecraft/nbt/NbtCompound;)V", at = @At("TAIL"))
    private void meldrite$readDiscounts(NbtCompound nbt, CallbackInfo ci) { meldrite$ignoreDiscounts = nbt.getBoolean("Paper.IgnoreDiscounts"); }
    @Inject(method = "toNbt", at = @At("RETURN"))
    private void meldrite$writeDiscounts(CallbackInfoReturnable<NbtCompound> cir) { if (meldrite$ignoreDiscounts) cir.getReturnValue().putBoolean("Paper.IgnoreDiscounts", true); }
}
