package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeServerListPingBridge;
import java.net.InetSocketAddress;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.query.QueryRequestC2SPacket;
import net.minecraft.server.ServerMetadata;
import net.minecraft.server.network.ServerQueryNetworkHandler;
import net.minecraft.text.Text;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keeps vanilla request state and duplicate-request handling; only replaces the response metadata. */
@Mixin(ServerQueryNetworkHandler.class)
public abstract class ServerQueryPingEventMixin implements NativeServerListPingBridge.HandshakeClient {
    @Shadow @Final private ClientConnection connection;
    @Shadow @Final private ServerMetadata metadata;
    @Unique private ServerMetadata meldrite$response;
    @Unique private int meldrite$clientProtocol = -1;
    @Unique private InetSocketAddress meldrite$virtualHost;

    @Override public void meldrite$setHandshake(int protocolVersion, String hostname, int port) {
        meldrite$clientProtocol = protocolVersion;
        meldrite$virtualHost = InetSocketAddress.createUnresolved(hostname, port);
    }

    @Override public InetSocketAddress getAddress() {
        if (connection.getAddress() instanceof InetSocketAddress socket) return socket;
        throw new IllegalStateException("Query connection address is not an InetSocketAddress: " + connection.getAddress());
    }

    @Override public int getProtocolVersion() { return meldrite$clientProtocol; }
    @Override public InetSocketAddress getVirtualHost() { return meldrite$virtualHost; }

    // Vanilla sets responseSent before constructing a response. Cancelling here keeps
    // its duplicate-request guard while preventing even an empty status packet.
    @Inject(method = "onRequest", at = @At(value = "FIELD",
            target = "Lnet/minecraft/server/network/ServerQueryNetworkHandler;responseSent:Z",
            opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER), cancellable = true)
    private void meldrite$dispatchPing(QueryRequestC2SPacket packet, CallbackInfo callback) {
        meldrite$response = NativeServerListPingBridge.response(metadata, this);
        if (meldrite$response == null) {
            connection.disconnect(Text.empty());
            callback.cancel();
        }
    }

    @ModifyArg(method = "onRequest", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/packet/s2c/query/QueryResponseS2CPacket;<init>(Lnet/minecraft/server/ServerMetadata;)V"), index = 0)
    private ServerMetadata meldrite$pingMetadata(ServerMetadata metadata) {
        return java.util.Objects.requireNonNull(meldrite$response, "Status response was not prepared");
    }
}
