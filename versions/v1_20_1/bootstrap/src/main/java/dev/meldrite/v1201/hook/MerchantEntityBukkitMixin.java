package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeMerchantAccess;
import net.minecraft.entity.passive.MerchantEntity;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftMerchant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MerchantEntity.class)
public abstract class MerchantEntityBukkitMixin implements NativeMerchantAccess {
    @Unique private CraftMerchant meldrite$merchant;
    @Override public CraftMerchant getCraftMerchant() {
        if (meldrite$merchant == null) meldrite$merchant = new CraftMerchant((MerchantEntity) (Object) this);
        return meldrite$merchant;
    }
}
