package dev.meldrite.v1201.hook;

import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.village.Merchant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MerchantScreenHandler.class)
public interface MerchantScreenHandlerAccess {
    @Accessor("merchant") Merchant meldrite$getMerchant();
}
