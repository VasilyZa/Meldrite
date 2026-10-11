package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeEntityExtensions;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.server.world.ServerWorld$ServerEntityHandler")
public abstract class ServerEntityTrackingMixin {
    @Inject(method = "startTracking(Lnet/minecraft/entity/Entity;)V", at = @At("HEAD"))
    private void meldrite$startTracking(Entity entity, CallbackInfo result) { NativeEntityExtensions.tracking(entity, true); }
    @Inject(method = "stopTracking(Lnet/minecraft/entity/Entity;)V", at = @At("HEAD"))
    private void meldrite$stopTracking(Entity entity, CallbackInfo result) { NativeEntityExtensions.tracking(entity, false); }
}
