package dev.meldrite.v1201.hook;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import dev.meldrite.v1201.compat.NativePlayerRespawn;
import java.util.Optional;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerManager.class)
public abstract class PlayerRespawnLifecycleMixin {
    @WrapMethod(method = "respawnPlayer")
    private ServerPlayerEntity meldrite$respawnLifecycle(ServerPlayerEntity player, boolean alive, Operation<ServerPlayerEntity> original) {
        return NativePlayerRespawn.lifecycle(player, alive, () -> original.call(player, alive));
    }

    // 1.20.1 selects and moves the replacement before this first getWorld call;
    // level properties, dimension packets, world info and native insertion follow it.
    @Inject(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getWorld()Lnet/minecraft/world/World;", ordinal = 0))
    private void meldrite$beforeRespawnPackets(ServerPlayerEntity player, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> result,
            @Local(index = 6) ServerWorld spawnWorld, @Local(index = 7) Optional<Vec3d> respawnPosition,
            @Local(index = 8) LocalRef<ServerWorld> destinationWorld, @Local(index = 9) ServerPlayerEntity replacement) {
        destinationWorld.set(NativePlayerRespawn.prepare(replacement, destinationWorld.get(), spawnWorld, respawnPosition));
    }
}
