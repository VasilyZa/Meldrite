package dev.meldrite.v1201.hook;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.meldrite.v1201.compat.NativeLivingPickup;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemEntity.class)
public abstract class PlayerItemPickupMixin {
    @WrapMethod(method = "onPlayerCollision")
    private void meldrite$pickup(PlayerEntity player, Operation<Void> original) {
        NativeLivingPickup.pickup(player, () -> original.call(player));
    }
}
