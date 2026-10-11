package dev.meldrite.v1201.hook;

import dev.meldrite.bridge.ActionDecision;
import dev.meldrite.v1201.bridge.BlockBreakBridge;
import dev.meldrite.v1201.bridge.HookGateway;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Thin native ingress; all event construction and policy remain in the profile. */
@Mixin(ServerPlayerInteractionManager.class)
public abstract class PlayerBreakMixin {
    @Shadow @Final protected ServerPlayerEntity player;

    @Inject(method = "tryBreakBlock", at = @At("HEAD"), cancellable = true)
    private void meldrite$beforeBreak(BlockPos position, CallbackInfoReturnable<Boolean> callback) {
        ActionDecision decision = HookGateway.current().before(player, position);
        if (BlockBreakBridge.apply(player, position, decision)) callback.setReturnValue(false);
    }
}
