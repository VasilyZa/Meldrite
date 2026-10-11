package dev.meldrite.v1201.hook;

import net.minecraft.village.MerchantInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MerchantInventory.class)
public interface MerchantInventoryAccess {
    @Accessor("offerIndex") int meldrite$getOfferIndex();
}
