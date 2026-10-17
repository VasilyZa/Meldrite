package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativePlayerDrop;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Adds Paper's four-argument drop ABI and applies it to native three-argument drops. */
@Mixin(PlayerEntity.class)
public abstract class PlayerDropItemMixin {
    /** Paper ABI name; this overload does not exist in the 1.20.1 native class. */
    public ItemEntity drop(ItemStack stack, boolean throwRandomly, boolean retainOwnership, boolean callEvent) {
        return NativePlayerDrop.drop((PlayerEntity) (Object) this, stack, throwRandomly, retainOwnership,
                callEvent, ((PlayerEntity) (Object) this).getRandom());
    }

    @Inject(method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;",
            at = @At("HEAD"), cancellable = true, require = 1)
    private void meldrite$nativeDrop(ItemStack stack, boolean throwRandomly, boolean retainOwnership,
            CallbackInfoReturnable<ItemEntity> result) {
        result.setReturnValue(NativePlayerDrop.drop((PlayerEntity) (Object) this, stack, throwRandomly,
                retainOwnership, true, ((PlayerEntity) (Object) this).getRandom()));
    }
}
