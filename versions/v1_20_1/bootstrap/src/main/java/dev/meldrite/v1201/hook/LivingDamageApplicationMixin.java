package dev.meldrite.v1201.hook;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.meldrite.v1201.compat.NativeDamageEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingDamageApplicationMixin {
    @WrapOperation(method = "applyDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;applyArmorToDamage(Lnet/minecraft/entity/damage/DamageSource;F)F"))
    private float meldrite$armor(LivingEntity entity, DamageSource source, float amount, Operation<Float> original) { var state = NativeDamageEvents.active(entity, source); return state == null ? original.call(entity, source, amount) : NativeDamageEvents.armorStage(state); }
    @WrapOperation(method = "applyDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;modifyAppliedDamage(Lnet/minecraft/entity/damage/DamageSource;F)F"))
    private float meldrite$magic(LivingEntity entity, DamageSource source, float amount, Operation<Float> original) { var state = NativeDamageEvents.active(entity, source); return state == null ? original.call(entity, source, amount) : NativeDamageEvents.magicStage(state); }
    @WrapOperation(method = "applyDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getAbsorptionAmount()F", ordinal = 0))
    private float meldrite$absorption(LivingEntity entity, Operation<Float> original) { var state = NativeDamageEvents.activeFor(entity); return state == null ? original.call(entity) : NativeDamageEvents.absorption(state); }
    @WrapOperation(method = "applyDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setAbsorptionAmount(F)V", ordinal = 0))
    private void meldrite$consumeAbsorption(LivingEntity entity, float amount, Operation<Void> original) { var state = NativeDamageEvents.activeFor(entity); original.call(entity, state == null ? amount : Math.max(0, entity.getAbsorptionAmount() - NativeDamageEvents.absorption(state))); }
    @WrapOperation(method = "applyDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setAbsorptionAmount(F)V", ordinal = 1))
    private void meldrite$alreadyConsumedAbsorption(LivingEntity entity, float amount, Operation<Void> original) { if (NativeDamageEvents.activeFor(entity) == null) original.call(entity, amount); }
}
