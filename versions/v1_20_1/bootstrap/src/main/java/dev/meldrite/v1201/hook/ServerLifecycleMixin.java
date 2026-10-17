package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.bridge.HookGateway;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Fallback cleanup for servers that bypass the Fabric lifecycle callback. */
@Mixin(MinecraftServer.class)
public abstract class ServerLifecycleMixin {
    @Unique private volatile boolean meldrite$shutdownStarted;

    @Inject(method = "shutdown", at = @At("HEAD"), require = 1)
    private void meldrite$markShutdownStarted(CallbackInfo callback) {
        meldrite$shutdownStarted = true;
    }

    /** Paper-compatible lifecycle state set as soon as native shutdown begins. */
    public final boolean hasStopped() {
        return meldrite$shutdownStarted;
    }

    @Inject(method = "shutdown", at = @At("TAIL"))
    private void meldrite$shutdown(CallbackInfo callback) {
        HookGateway gateway = null;
        try { gateway = HookGateway.current(); } catch (IllegalStateException ignored) { }
        if (gateway != null) gateway.close();
    }
}
