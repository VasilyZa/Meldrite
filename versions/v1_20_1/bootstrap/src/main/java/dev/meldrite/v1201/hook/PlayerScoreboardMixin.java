package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeScoreboardBinding;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerManager.class)
abstract class PlayerScoreboardMixin {
    @Inject(method = "sendScoreboard(Lnet/minecraft/scoreboard/ServerScoreboard;Lnet/minecraft/server/network/ServerPlayerEntity;)V", at = @At("HEAD"), cancellable = true)
    private void meldrite$assignedScoreboard(ServerScoreboard board, ServerPlayerEntity player, CallbackInfo callback) {
        if (NativeScoreboardBinding.synchronizeAssignedPlayer(player)) callback.cancel();
    }

    @Inject(method = "respawnPlayer(Lnet/minecraft/server/network/ServerPlayerEntity;Z)Lnet/minecraft/server/network/ServerPlayerEntity;", at = @At("RETURN"))
    private void meldrite$restoreScoreboard(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> callback) {
        NativeScoreboardBinding.synchronizeAssignedPlayer(callback.getReturnValue());
    }

    @Inject(method = "remove(Lnet/minecraft/server/network/ServerPlayerEntity;)V", at = @At("TAIL"))
    private void meldrite$forgetScoreboard(ServerPlayerEntity player, CallbackInfo callback) {
        NativeScoreboardBinding.removePlayer(player);
    }
}
