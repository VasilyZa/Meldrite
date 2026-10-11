package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeEntityTeleport;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class PlayerTeleportFacadeMixin {
    @Inject(method = "setServerWorld", at = @At("TAIL"))
    private void meldrite$rebindWorld(ServerWorld world, CallbackInfo callback) {
        NativeEntityTeleport.playerWorldChanged((ServerPlayerEntity) (Object) this);
    }
}
