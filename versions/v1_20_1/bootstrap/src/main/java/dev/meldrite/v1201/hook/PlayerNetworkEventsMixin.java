package dev.meldrite.v1201.hook;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.meldrite.v1201.compat.NativeEntityExtensions;
import dev.meldrite.v1201.compat.NativePlayerChat;
import dev.meldrite.v1201.compat.NativePlayerMovement;
import java.util.Set;
import net.minecraft.network.message.LastSeenMessageList;
import net.minecraft.network.message.SignedCommandArguments;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class PlayerNetworkEventsMixin {
    @Shadow public ServerPlayerEntity player;
    @Shadow @Final private MinecraftServer server;
    @Unique private final NativePlayerMovement meldrite$movement = new NativePlayerMovement();
    @Unique private Location meldrite$packetFrom;

    // Vanilla has already validated and acknowledged this packet and queued it on
    // the server thread. Preserve its parse/signature/execution path when unchanged.
    @WrapMethod(method = "handleCommandExecution")
    private void meldrite$preprocessCommand(CommandExecutionC2SPacket packet, LastSeenMessageList messages, Operation<Void> original) {
        if (PlayerCommandPreprocessEvent.getHandlerList().getRegisteredListeners().length == 0) {
            original.call(packet, messages);
            return;
        }
        String command = NativePlayerChat.preprocessCommand((Player) NativeEntityExtensions.bukkit(player), packet.command());
        if (command == null) return;
        if (command.equals(packet.command())) original.call(packet, messages);
        else server.getCommandManager().executeWithPrefix(player.getCommandSource().withSignedArguments(SignedCommandArguments.EMPTY), command);
    }

    @Inject(method = "onPlayerMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V", shift = At.Shift.AFTER), require = 1)
    private void meldrite$captureMoveOrigin(PlayerMoveC2SPacket packet, CallbackInfo callback) {
        meldrite$packetFrom = null;
        if (PlayerMoveEvent.getHandlerList().getRegisteredListeners().length == 0) {
            meldrite$movement.reset();
            return;
        }
        if (!player.notInAnyWorld && !player.isRemoved()) {
            meldrite$packetFrom = NativeEntityExtensions.bukkit(player).getLocation();
        }
    }

    // Both accepted walking and mounted look packets commit their final position
    // here. Rejected, invalid and unacknowledged packets never fire a move event.
    @Inject(method = "onPlayerMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;updatePositionAndAngles(DDDFF)V"), cancellable = true, require = 1)
    private void meldrite$moveEvent(PlayerMoveC2SPacket packet, CallbackInfo callback) {
        Location before = meldrite$packetFrom;
        if (before == null) return;
        var facade = (Player) NativeEntityExtensions.bukkit(player);
        Location to = before.clone();
        if (!player.hasVehicle()) {
            to.setX(packet.getX(before.getX()));
            to.setY(packet.getY(before.getY()));
            to.setZ(packet.getZ(before.getZ()));
        }
        to.setYaw(packet.getYaw(before.getYaw()));
        to.setPitch(packet.getPitch(before.getPitch()));
        // Collision validation temporarily moved the native entity. Listeners
        // must observe the original position, just as in CraftBukkit.
        player.updatePositionAndAngles(before.getX(), before.getY(), before.getZ(), before.getYaw(), before.getPitch());
        if (!meldrite$movement.move(facade, before, to,
                from -> NativeEntityExtensions.teleport((ServerPlayNetworkHandler) (Object) this, from))) callback.cancel();
    }

    @Inject(method = "requestTeleport(DDDFFLjava/util/Set;)V", at = @At("TAIL"), require = 1)
    private void meldrite$resetMoveOrigin(double x, double y, double z, float yaw, float pitch, Set<PositionFlag> flags, CallbackInfo callback) {
        meldrite$packetFrom = null;
        meldrite$movement.reset();
    }
}
