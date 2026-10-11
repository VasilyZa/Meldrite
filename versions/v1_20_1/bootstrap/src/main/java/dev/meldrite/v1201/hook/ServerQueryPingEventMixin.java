package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeServerListPingBridge;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.ServerMetadata;
import net.minecraft.server.network.ServerQueryNetworkHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Keeps vanilla request state and duplicate-request handling; only replaces the response metadata. */
@Mixin(ServerQueryNetworkHandler.class)
public abstract class ServerQueryPingEventMixin {
    @Shadow @Final private ClientConnection connection;

    @ModifyArg(method = "onRequest", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/packet/s2c/query/QueryResponseS2CPacket;<init>(Lnet/minecraft/server/ServerMetadata;)V"), index = 0)
    private ServerMetadata meldrite$pingMetadata(ServerMetadata metadata) {
        return NativeServerListPingBridge.response(metadata, connection.getAddress());
    }
}
