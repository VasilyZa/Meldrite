package dev.meldrite.v1201.hook;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.meldrite.v1201.compat.NativeDamageEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(StatusEffect.class)
public abstract class StatusEffectDamageHealingMixin {
    @WrapOperation(method = {"applyUpdateEffect", "applyInstantEffect"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;heal(F)V"))
    private void meldrite$reason(LivingEntity entity, float amount, Operation<Void> original) {
        var reason = (Object) this == StatusEffects.REGENERATION ? org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason.MAGIC_REGEN : org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason.MAGIC;
        NativeDamageEvents.withHealReason(reason, () -> original.call(entity, amount));
    }
    @WrapOperation(method = "applyUpdateEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
    private boolean meldrite$poison(LivingEntity entity, DamageSource source, float amount, Operation<Boolean> original) {
        if ((Object) this == StatusEffects.POISON) {
            source = new DamageSource(source.getTypeRegistryEntry()); ((dev.meldrite.v1201.compat.PaperDamageSourceAccess) source).meldrite$setPoison();
        }
        return original.call(entity, source, amount);
    }
}
