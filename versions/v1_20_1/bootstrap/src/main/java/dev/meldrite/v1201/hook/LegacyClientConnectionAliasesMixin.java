package dev.meldrite.v1201.hook;

import io.netty.channel.Channel;
import net.minecraft.network.ClientConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** Legacy connection accessors backed by the native connection. */
@Mixin(ClientConnection.class)
public abstract class LegacyClientConnectionAliasesMixin {
    @Shadow private Channel channel;

    public Channel channel() {
        return channel;
    }

    public Channel getChannel() {
        return channel;
    }
}
