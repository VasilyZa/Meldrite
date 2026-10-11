package dev.meldrite.v1201.hook;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.meldrite.v1201.compat.NativeDamageEvents;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(net.minecraft.entity.player.HungerManager.class)
public abstract class HungerHealingReasonMixin {
    @WrapOperation(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;heal(F)V", ordinal = 0), require = 1)
    private void meldrite$fastReason(PlayerEntity entity, float amount, Operation<Void> original) { NativeDamageEvents.withHealReason(org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason.SATIATED, true, () -> original.call(entity, amount)); }
    @WrapOperation(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;heal(F)V", ordinal = 1), require = 1)
    private void meldrite$reason(PlayerEntity entity, float amount, Operation<Void> original) { NativeDamageEvents.withHealReason(org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason.SATIATED, () -> original.call(entity, amount)); }
}
