package dev.meldrite.v1201.hook;
import dev.meldrite.v1201.compat.PaperPlayerAccess;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
/** Keeps vanilla broadcasts from overwriting a player's environment override. */
@Mixin(ServerPlayNetworkHandler.class)
public abstract class PlayerEnvironmentPacketMixin {
    @Shadow public ServerPlayerEntity player;
    @Redirect(method = "onDisconnected", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/text/Text;Z)V"))
    private void meldrite$kickLeaveMessage(net.minecraft.server.PlayerManager manager, net.minecraft.text.Text message, boolean overlay) {
        var state = ((PaperPlayerAccess) player).meldrite$playerState();
        if (!state.kickLeaveMessageSet) { manager.broadcast(message, overlay); return; }
        state.kickLeaveMessageSet = false;
        var leave = state.kickLeaveMessage; state.kickLeaveMessage = null;
        if (leave != null) manager.broadcast(net.minecraft.text.Text.Serializer.fromJson(net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson().serialize(leave)), overlay);
    }
    @ModifyVariable(method = "sendPacket(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;)V", at = @At("HEAD"), argsOnly = true)
    private Packet<?> meldrite$environment(Packet<?> packet) {
        var state = ((PaperPlayerAccess) player).meldrite$playerState();
        if (packet instanceof net.minecraft.network.packet.s2c.play.PlayerListS2CPacket list) {
            var entries = list.getEntries().stream().filter(entry -> {
                var other = player.server.getPlayerManager().getPlayer(entry.profileId());
                return other == null || dev.meldrite.v1201.compat.NativePlayerVisibility.canSee(player, other);
            }).toList();
            if (entries.size() != list.getEntries().size()) return ((dev.meldrite.v1201.compat.NativePlayerListPacketAccess) list).meldrite$withEntries(entries);
        }
        if (packet instanceof net.minecraft.network.packet.s2c.play.ChunkLoadDistanceS2CPacket || packet instanceof net.minecraft.network.packet.s2c.play.SimulationDistanceS2CPacket)
            return dev.meldrite.v1201.compat.NativePlayerEnvironment.personalize(packet, state, 0, true);
        if (!(packet instanceof WorldTimeUpdateS2CPacket) && !(packet instanceof GameStateChangeS2CPacket))
            return dev.meldrite.v1201.compat.NativePlayerBorder.personalize(player, packet);
        var world = player.getServerWorld();
        return dev.meldrite.v1201.compat.NativePlayerEnvironment.personalize(packet, state, world.getTimeOfDay(),
                world.getGameRules().getBoolean(net.minecraft.world.GameRules.DO_DAYLIGHT_CYCLE));
    }
}
