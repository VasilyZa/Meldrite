package dev.meldrite.v1201.hook;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** Stable connection and latency names used by 1.20-era NMS integrations. */
@Mixin(ServerPlayNetworkHandler.class)
public abstract class LegacyNetworkHandlerAliasesMixin implements dev.meldrite.v1201.compat.NativePlayerConnectionAccess {
    @Shadow private ClientConnection connection;
    @Shadow public net.minecraft.server.network.ServerPlayerEntity player;

    @Override public ClientConnection meldrite$connection() {
        return connection;
    }

    public ClientConnection connection() {
        return connection;
    }

    public int latency() {
        return player == null ? 0 : player.pingMilliseconds;
    }

    /** Modern name used by packet-only teleport integrations; delegates to the native request path. */
    public void internalTeleport(double x, double y, double z, float yaw, float pitch) {
        ((ServerPlayNetworkHandler) (Object) this).requestTeleport(x, y, z, yaw, pitch);
    }
}
