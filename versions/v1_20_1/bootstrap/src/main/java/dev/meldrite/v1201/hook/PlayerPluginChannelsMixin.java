package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeEntityExtensions;
import dev.meldrite.v1201.compat.PaperPlayerAccess;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class PlayerPluginChannelsMixin {
    @Shadow public ServerPlayerEntity player;
    @Inject(method = "onCustomPayload", at = @At("HEAD"))
    private void meldrite$payload(CustomPayloadC2SPacket packet, CallbackInfo ci) {
        var channel = packet.getChannel().toString(); var data = new net.minecraft.network.PacketByteBuf(packet.getData().duplicate());
        var state = ((PaperPlayerAccess) player).meldrite$playerState();
        if (channel.equals("minecraft:brand")) {
            try { state.clientBrand = data.readString(32767); }
            catch (RuntimeException malformedBrand) { /* Observing Bukkit data must preserve native/mod payload handling. */ }
            return;
        }
        byte[] bytes = new byte[data.readableBytes()]; data.readBytes(bytes);
        if (channel.equals("minecraft:register") || channel.equals("minecraft:unregister")) {
            for (var name : new String(bytes, java.nio.charset.StandardCharsets.UTF_8).split("\u0000")) {
                if (name.isEmpty()) continue;
                try {
                    var normalized = org.bukkit.plugin.messaging.StandardMessenger.validateAndCorrectChannel(name);
                    if (normalized.equals("BungeeCord")) normalized = "bungeecord:main";
                    if (channel.equals("minecraft:register")) state.channels.add(normalized); else state.channels.remove(normalized);
                } catch (IllegalArgumentException nativeOnlyChannel) { /* Keep channels outside the Bukkit format available to mods. */ }
            }
        } else {
            player.server.execute(() -> {
                var facade = (org.bukkit.entity.Player) NativeEntityExtensions.bukkit(player);
                var messenger = facade.getServer().getMessenger();
                String apiChannel = channel.equals("bungeecord:main") ? "BungeeCord" : channel;
                try {
                    if (messenger.getIncomingChannelRegistrations(apiChannel).isEmpty()) return;
                } catch (IllegalArgumentException nativeOnlyChannel) { return; }
                messenger.dispatchIncomingMessage(facade, apiChannel, bytes);
            });
        }
    }
}
