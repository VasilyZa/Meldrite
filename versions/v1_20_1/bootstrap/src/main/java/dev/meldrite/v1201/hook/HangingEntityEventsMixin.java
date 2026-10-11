package dev.meldrite.v1201.hook;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(AbstractDecorationEntity.class)
public abstract class HangingEntityEventsMixin {
    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/decoration/AbstractDecorationEntity;discard()V"), cancellable = true)
    private void meldrite$physics(CallbackInfo callback) {
        var entity = (AbstractDecorationEntity) (Object) this; var cause = entity.getWorld().getBlockState(entity.getBlockPos()).isAir() ? HangingBreakEvent.RemoveCause.PHYSICS : HangingBreakEvent.RemoveCause.OBSTRUCTION;
        if (!dev.meldrite.v1201.compat.NativeDecorationEvents.canBreak(entity, null, cause)) callback.cancel();
    }
    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/decoration/AbstractDecorationEntity;kill()V"), cancellable = true)
    private void meldrite$damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> callback) {
        var entity = (AbstractDecorationEntity) (Object) this;
        var cause = source.isIn(net.minecraft.registry.tag.DamageTypeTags.IS_EXPLOSION) ? HangingBreakEvent.RemoveCause.EXPLOSION : (source.getSource() != null || source.getAttacker() != null) ? HangingBreakEvent.RemoveCause.ENTITY : HangingBreakEvent.RemoveCause.DEFAULT;
        if (!dev.meldrite.v1201.compat.NativeDecorationEvents.canBreak(entity, source, cause)) callback.setReturnValue(true);
    }
    @Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/decoration/AbstractDecorationEntity;kill()V"), cancellable = true)
    private void meldrite$move(MovementType type, Vec3d movement, CallbackInfo callback) { if (!dev.meldrite.v1201.compat.NativeDecorationEvents.canBreak((AbstractDecorationEntity) (Object) this, null, HangingBreakEvent.RemoveCause.PHYSICS)) callback.cancel(); }
    @Inject(method = "addVelocity", at = @At("HEAD"), cancellable = true)
    private void meldrite$velocity(double x, double y, double z, CallbackInfo callback) { callback.cancel(); }
}
