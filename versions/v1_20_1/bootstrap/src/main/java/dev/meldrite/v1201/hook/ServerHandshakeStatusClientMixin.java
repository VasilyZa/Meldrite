package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeServerListPingBridge;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkState;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.server.network.ServerHandshakeNetworkHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Passes the actual status handshake to the query listener without retaining connections. */
@Mixin(ServerHandshakeNetworkHandler.class)
public abstract class ServerHandshakeStatusClientMixin {
    @Shadow @Final private ClientConnection connection;

    @Inject(method = "onHandshake", at = @At("RETURN"))
    private void meldrite$statusClient(HandshakeC2SPacket packet, CallbackInfo callback) {
        if (packet.getIntendedState() == NetworkState.STATUS
                && connection.getPacketListener() instanceof NativeServerListPingBridge.HandshakeClient client) {
            client.meldrite$setHandshake(packet.getProtocolVersion(), packet.getAddress(), packet.getPort());
        }
    }
}
