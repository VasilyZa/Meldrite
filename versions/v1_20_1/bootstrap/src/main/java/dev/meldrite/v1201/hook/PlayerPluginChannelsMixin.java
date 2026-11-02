package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeEntityExtensions;
import dev.meldrite.v1201.compat.PaperPlayerAccess;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.plugin.messaging.ChannelNameTooLongException;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.StandardMessenger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Fabric Networking uses priority 999 and may cancel this method after its own
// asynchronous handling. Observe first, without changing or retaining its buffer.
@Mixin(value = ServerPlayNetworkHandler.class, priority = 998)
public abstract class PlayerPluginChannelsMixin {
    @Shadow public ServerPlayerEntity player;
    @Inject(method = "onCustomPayload", at = @At("HEAD"))
    private void meldrite$payload(CustomPayloadC2SPacket packet, CallbackInfo ci) {
        var channel = packet.getChannel().toString();
        var data = new net.minecraft.network.PacketByteBuf(packet.getData().duplicate());
        if (channel.equals("minecraft:brand")) {
            String brand;
            try { brand = data.readString(32767); }
            catch (RuntimeException malformedBrand) { return; }
            player.server.execute(() -> ((PaperPlayerAccess) player).meldrite$playerState().clientBrand = brand);
            return;
        }
        byte[] bytes = new byte[data.readableBytes()]; data.readBytes(bytes);
        player.server.execute(() -> {
            var facade = (CraftPlayer) NativeEntityExtensions.bukkit(player);
            if (channel.equals("minecraft:register") || channel.equals("minecraft:unregister")) {
                for (var name : new String(bytes, java.nio.charset.StandardCharsets.UTF_8).split("\u0000")) {
                    if (name.isEmpty()) continue;
                    try { StandardMessenger.validateAndCorrectChannel(name); }
                    catch (IllegalArgumentException | ChannelNameTooLongException nativeOnlyChannel) { continue; }
                    if (channel.equals("minecraft:register")) facade.addChannel(name); else facade.removeChannel(name);
                }
            } else {
                var messenger = facade.getServer().getMessenger();
                try {
                    if (messenger.getIncomingChannelRegistrations(channel).isEmpty()) return;
                } catch (IllegalArgumentException | ChannelNameTooLongException nativeOnlyChannel) { return; }
                if (bytes.length > Messenger.MAX_MESSAGE_SIZE) {
                    facade.getServer().getLogger().warning("Ignoring oversized Bukkit plugin message on " + channel + ": " + bytes.length + " bytes");
                    return;
                }
                messenger.dispatchIncomingMessage(facade, channel, bytes);
            }
        });
    }
}
