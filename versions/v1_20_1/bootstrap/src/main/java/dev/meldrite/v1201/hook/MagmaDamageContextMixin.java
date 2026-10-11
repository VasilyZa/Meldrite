package dev.meldrite.v1201.hook;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.meldrite.v1201.compat.NativeDamageEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(net.minecraft.block.MagmaBlock.class)
public abstract class MagmaDamageContextMixin {
    @WrapOperation(method = "onSteppedOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
    private boolean meldrite$blockDamage(Entity entity, DamageSource source, float amount, Operation<Boolean> original, @Local(argsOnly = true) BlockPos position) { return NativeDamageEvents.withBlock(entity, position, () -> original.call(entity, source, amount)); }
}
