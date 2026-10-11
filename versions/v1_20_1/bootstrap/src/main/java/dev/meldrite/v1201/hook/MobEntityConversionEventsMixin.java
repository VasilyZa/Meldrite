package dev.meldrite.v1201.hook;
import com.llamalad7.mixinextras.sugar.Local;
import dev.meldrite.v1201.compat.NativeMobConversionEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(MobEntity.class)
public abstract class MobEntityConversionEventsMixin {
    @Inject(method = "convertTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"), cancellable = true)
    private <T extends MobEntity> void meldrite$conversion(EntityType<T> type, boolean equipment, CallbackInfoReturnable<T> cir, @Local MobEntity converted) {
        if (!NativeMobConversionEvents.allow((MobEntity) (Object) this, converted)) cir.setReturnValue(null);
    }
}
