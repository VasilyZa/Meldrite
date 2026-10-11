package dev.meldrite.v1201.hook;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.meldrite.v1201.compat.NativeDamageEvents;
import dev.meldrite.v1201.compat.NativeEntityExtensions;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityDamageEventsMixin {
    @WrapMethod(method = "damage")
    private boolean meldrite$damage(DamageSource source, float amount, Operation<Boolean> original) { return NativeDamageEvents.damage((LivingEntity) (Object) this, source, amount, original); }
    @Inject(method = "damage", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;despawnCounter:I", opcode = org.objectweb.asm.Opcodes.PUTFIELD), cancellable = true)
    private void meldrite$beforeSideEffects(DamageSource source, float amount, CallbackInfoReturnable<Boolean> callback) { if (!NativeDamageEvents.beforeDamage((LivingEntity) (Object) this, source, amount)) callback.setReturnValue(false); }
    @ModifyConstant(method = "damage", constant = @Constant(floatValue = 10F, ordinal = 0), require = 1)
    private float meldrite$cooldownThreshold(float vanilla) { return NativeEntityExtensions.state((LivingEntity) (Object) this).meldrite$getMaximumNoDamageTicks() / 2F; }
    @WrapOperation(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;blockedByShield(Lnet/minecraft/entity/damage/DamageSource;)Z"))
    private boolean meldrite$deferShield(LivingEntity entity, DamageSource source, Operation<Boolean> original) { return NativeDamageEvents.active(entity, source) == null && original.call(entity, source); }
    @WrapOperation(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;damageHelmet(Lnet/minecraft/entity/damage/DamageSource;F)V"))
    private void meldrite$helmet(LivingEntity entity, DamageSource source, float amount, Operation<Void> original) { if (NativeDamageEvents.active(entity, source) == null) original.call(entity, source, amount); }
    @WrapOperation(method = "applyArmorToDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;damageArmor(Lnet/minecraft/entity/damage/DamageSource;F)V"))
    private void meldrite$previewArmor(LivingEntity entity, DamageSource source, float amount, Operation<Void> original) { if (!NativeDamageEvents.preview(entity)) original.call(entity, source, amount); }
    @WrapOperation(method = "modifyAppliedDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/entity/effect/StatusEffect;)Z"))
    private boolean meldrite$separateResistance(LivingEntity entity, StatusEffect effect, Operation<Boolean> original) { return !(effect == StatusEffects.RESISTANCE && NativeDamageEvents.previewMagic(entity)) && original.call(entity, effect); }
    @WrapMethod(method = "heal(F)V")
    private void meldrite$heal(float amount, Operation<Void> original) { NativeDamageEvents.heal((LivingEntity) (Object) this, amount, NativeDamageEvents.healReason(), NativeDamageEvents.fastHeal(), original); }
    public void heal(float amount, org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason reason) { heal(amount, reason, false); }
    public void heal(float amount, org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason reason, boolean fast) {
        NativeDamageEvents.withHealReason(reason, fast, () -> ((LivingEntity) (Object) this).heal(amount));
    }
}
