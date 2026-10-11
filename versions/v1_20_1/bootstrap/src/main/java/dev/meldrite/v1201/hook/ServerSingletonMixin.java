package dev.meldrite.v1201.hook;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Exposes the running Fabric server through the legacy NMS singleton API. */
@Mixin(MinecraftServer.class)
public abstract class ServerSingletonMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void meldrite$capture(CallbackInfo callback) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        dev.meldrite.v1201.runtime.ServerSingletonAccess.bind(server);
    }
}
