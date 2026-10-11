package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.PaperPlayerAccess;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.SleepManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Ignored sleepers count toward the threshold, but a real sleeper must start the night skip. */
@Mixin(SleepManager.class)
public abstract class PlayerSleepStatusMixin {
    @Unique private boolean meldrite$hasIgnoredPlayers;
    @Unique private boolean meldrite$hasActualSleeper;
    @Shadow public abstract int getNightSkippingRequirement(int percentage);
    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;isSleeping()Z"))
    private boolean meldrite$countIgnored(ServerPlayerEntity player) {
        return player.isSleeping() || ((PaperPlayerAccess) player).meldrite$playerState().sleepingIgnored;
    }
    @Inject(method = "update", at = @At("RETURN"))
    private void meldrite$actualSleeper(java.util.List<ServerPlayerEntity> players, CallbackInfoReturnable<Boolean> ci) {
        meldrite$hasIgnoredPlayers = false;
        meldrite$hasActualSleeper = false;
        for (var player : players) if (!player.isSpectator()) {
            meldrite$hasIgnoredPlayers |= ((PaperPlayerAccess) player).meldrite$playerState().sleepingIgnored;
            meldrite$hasActualSleeper |= player.isSleeping();
        }
    }
    @Inject(method = "canSkipNight", at = @At("RETURN"), cancellable = true)
    private void meldrite$requireActualSleeper(int percentage, CallbackInfoReturnable<Boolean> ci) {
        if (meldrite$hasIgnoredPlayers && !meldrite$hasActualSleeper && ci.getReturnValue()) ci.setReturnValue(false);
    }
    @Inject(method = "canResetTime", at = @At("RETURN"), cancellable = true)
    private void meldrite$deepSleep(int percentage, java.util.List<ServerPlayerEntity> players, CallbackInfoReturnable<Boolean> ci) {
        int count = 0; boolean anyDeepSleep = false; boolean anyIgnored = false;
        for (var player : players) {
            boolean deep = player.canResetTimeBySleeping(); anyDeepSleep |= deep;
            boolean ignored = !player.isSpectator() && ((PaperPlayerAccess) player).meldrite$playerState().sleepingIgnored;
            anyIgnored |= ignored;
            if (deep || ignored) count++;
        }
        if (anyIgnored && !ci.getReturnValue() && anyDeepSleep && count >= getNightSkippingRequirement(percentage)) ci.setReturnValue(true);
    }
}
