package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeEntityExtensions;
import dev.meldrite.v1201.compat.NativePlayerSessionEvents;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Async roster readers see only players whose Bukkit facade is already published. */
@Mixin(PlayerManager.class)
public abstract class PlayerFacadePublicationMixin {
    @WrapMethod(method = "onPlayerConnect")
    private void meldrite$joinEvent(ClientConnection connection, ServerPlayerEntity player, Operation<Void> original) {
        NativePlayerSessionEvents.connect(player, () -> original.call(connection, player));
    }
    @WrapMethod(method = "remove")
    private void meldrite$quitEvent(ServerPlayerEntity player, Operation<Void> original) {
        NativePlayerSessionEvents.remove(player, () -> original.call(player));
    }
    @WrapMethod(method = "broadcast(Lnet/minecraft/text/Text;Z)V")
    private void meldrite$sessionNotification(net.minecraft.text.Text message, boolean overlay, Operation<Void> original) {
        NativePlayerSessionEvents.broadcast(message, overlay, text -> original.call(text, overlay));
    }
    @Inject(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
    private void meldrite$publishBeforeOnlineRoster(ClientConnection connection, ServerPlayerEntity player, CallbackInfo callback) {
        if (NativeEntityExtensions.eventsAvailable()) NativeEntityExtensions.bukkit(player);
    }
}
