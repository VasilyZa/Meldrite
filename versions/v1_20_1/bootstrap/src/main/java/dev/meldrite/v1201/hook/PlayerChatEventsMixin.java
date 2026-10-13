package dev.meldrite.v1201.hook;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.meldrite.v1201.compat.NativeEntityExtensions;
import dev.meldrite.v1201.compat.NativePlayerChat;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.filter.FilteredMessage;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/** Bridges validated chat inside vanilla's per-connection message chain. */
@Mixin(ServerPlayNetworkHandler.class)
public abstract class PlayerChatEventsMixin {
    @Shadow public ServerPlayerEntity player;

    // Both filter/decorator futures complete before events. The resulting future
    // remains in vanilla's queue, including synchronous events and native delivery.
    @WrapOperation(method = "method_45167", at = @At(value = "INVOKE",
            target = "Ljava/util/concurrent/CompletableFuture;thenAcceptAsync(Ljava/util/function/Consumer;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    private CompletableFuture<Void> meldrite$chatEvents(CompletableFuture<Void> ready,
            Consumer<? super Void> continuation, Executor primary,
            Operation<CompletableFuture<Void>> original,
            @Local(argsOnly = true, ordinal = 0) CompletableFuture<FilteredMessage> filtered,
            @Local(argsOnly = true, ordinal = 1) CompletableFuture<Text> decorated,
            @Local(argsOnly = true) SignedMessage signed) {
        if (!NativeEntityExtensions.eventsAvailable() || !NativePlayerChat.hasListeners()) {
            return original.call(ready, continuation, primary);
        }
        return NativePlayerChat.chain(player, ready, continuation, primary, filtered, decorated, signed);
    }

    // Keep native spam accounting and packet validation. Only replace the final
    // broadcast when a chat listener actually changes the event's output.
    @WrapOperation(method = "handleDecoratedMessage", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/network/message/SignedMessage;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/network/message/MessageType$Parameters;)V"))
    private void meldrite$broadcastChat(PlayerManager manager, SignedMessage message,
            ServerPlayerEntity sender, MessageType.Parameters parameters, Operation<Void> original) {
        NativePlayerChat.broadcast(sender, message, parameters,
                () -> original.call(manager, message, sender, parameters));
    }
}
