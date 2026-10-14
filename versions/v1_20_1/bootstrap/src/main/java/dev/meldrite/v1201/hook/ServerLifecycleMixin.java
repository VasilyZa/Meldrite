package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.bridge.HookGateway;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Fallback cleanup for servers that bypass the Fabric lifecycle callback. */
@Mixin(MinecraftServer.class)
public abstract class ServerLifecycleMixin {
    @Inject(method = "shutdown", at = @At("TAIL"))
    private void meldrite$shutdown(CallbackInfo callback) {
        HookGateway gateway = null;
        try { gateway = HookGateway.current(); } catch (IllegalStateException ignored) { }
        if (gateway != null) gateway.close();
    }
}
