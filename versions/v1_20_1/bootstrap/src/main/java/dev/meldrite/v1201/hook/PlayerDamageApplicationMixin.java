package dev.meldrite.v1201.hook;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.meldrite.v1201.compat.NativeDamageEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntity.class)
public abstract class PlayerDamageApplicationMixin {
    @WrapOperation(method = "applyDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;applyArmorToDamage(Lnet/minecraft/entity/damage/DamageSource;F)F"))
    private float meldrite$armor(PlayerEntity entity, DamageSource source, float amount, Operation<Float> original) { var state = NativeDamageEvents.active(entity, source); return state == null ? original.call(entity, source, amount) : NativeDamageEvents.armorStage(state); }
    @WrapOperation(method = "applyDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;modifyAppliedDamage(Lnet/minecraft/entity/damage/DamageSource;F)F"))
    private float meldrite$magic(PlayerEntity entity, DamageSource source, float amount, Operation<Float> original) { var state = NativeDamageEvents.active(entity, source); return state == null ? original.call(entity, source, amount) : NativeDamageEvents.magicStage(state); }
    @WrapOperation(method = "applyDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getAbsorptionAmount()F", ordinal = 0))
    private float meldrite$absorption(PlayerEntity entity, Operation<Float> original) { var state = NativeDamageEvents.activeFor(entity); return state == null ? original.call(entity) : NativeDamageEvents.absorption(state); }
    @WrapOperation(method = "applyDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setAbsorptionAmount(F)V"))
    private void meldrite$consumeAbsorption(PlayerEntity entity, float amount, Operation<Void> original) { var state = NativeDamageEvents.activeFor(entity); original.call(entity, state == null ? amount : Math.max(0, entity.getAbsorptionAmount() - NativeDamageEvents.absorption(state))); }
}
