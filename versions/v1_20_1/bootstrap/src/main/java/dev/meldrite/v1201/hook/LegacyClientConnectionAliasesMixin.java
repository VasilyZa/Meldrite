package dev.meldrite.v1201.hook;

import io.netty.channel.Channel;
import net.minecraft.network.ClientConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** Legacy connection accessors backed by the native connection. */
@Mixin(ClientConnection.class)
public abstract class LegacyClientConnectionAliasesMixin implements dev.meldrite.v1201.compat.NativeConnectionAccess {
    @Shadow private Channel channel;
    @Shadow private boolean disconnected;

    @Override public Channel meldrite$channel() {
        return channel;
    }

    @Override public boolean meldrite$disconnectionHandled() {
        return disconnected;
    }

    public Channel channel() {
        return channel;
    }

    public Channel getChannel() {
        return channel;
    }
}
