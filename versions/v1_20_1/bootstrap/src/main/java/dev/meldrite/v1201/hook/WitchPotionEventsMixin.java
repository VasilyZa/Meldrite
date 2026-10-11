package dev.meldrite.v1201.hook;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.meldrite.v1201.compat.NativeWitchBehavior;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WitchEntity.class)
public abstract class WitchPotionEventsMixin {
    @WrapOperation(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/potion/PotionUtil;setPotion(Lnet/minecraft/item/ItemStack;Lnet/minecraft/potion/Potion;)Lnet/minecraft/item/ItemStack;"))
    private ItemStack meldrite$ready(ItemStack stack, Potion potion, Operation<ItemStack> original) {
        return NativeWitchBehavior.prepare((WitchEntity) (Object) this, original.call(stack, potion));
    }
    @WrapOperation(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/potion/PotionUtil;getPotionEffects(Lnet/minecraft/item/ItemStack;)Ljava/util/List;"))
    private java.util.List<StatusEffectInstance> meldrite$consume(ItemStack potion, Operation<java.util.List<StatusEffectInstance>> original) {
        return NativeWitchBehavior.consume((WitchEntity) (Object) this, potion);
    }
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void meldrite$throw(LivingEntity target, float charge, CallbackInfo callback) {
        NativeWitchBehavior.attack((WitchEntity) (Object) this, target);
        callback.cancel();
    }
}
