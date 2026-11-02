package dev.meldrite.v1201.hook;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.meldrite.v1201.compat.NativeCommandSuggestions;
import dev.meldrite.v1201.compat.NativeCommandTreeEvents;
import dev.meldrite.v1201.compat.NativeEntityExtensions;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class CommandSuggestionsEventsMixin {
    @Shadow public ServerPlayerEntity player;

    @WrapOperation(method = "onRequestCommandCompletions", at = @At(value = "INVOKE",
            target = "Lcom/mojang/brigadier/CommandDispatcher;getCompletionSuggestions(Lcom/mojang/brigadier/ParseResults;)Ljava/util/concurrent/CompletableFuture;"), require = 1)
    private java.util.concurrent.CompletableFuture<Suggestions> meldrite$clientProviders(
            CommandDispatcher<ServerCommandSource> dispatcher, ParseResults<ServerCommandSource> parsed,
            Operation<java.util.concurrent.CompletableFuture<Suggestions>> original) {
        return NativeCommandSuggestions.complete(player, dispatcher, parsed, original.call(dispatcher, parsed));
    }

    @Inject(method = "onRequestCommandCompletions", at = @At("HEAD"), cancellable = true, require = 1)
    private void meldrite$disabledSuggestions(RequestCommandCompletionsC2SPacket request, CallbackInfo callback) {
        if (org.spigotmc.SpigotConfig.tabComplete < 0) callback.cancel();
    }

    // Yarn's stable intermediary name for the completion future's (request, suggestions) callback.
    @Inject(method = "method_14365", at = @At("HEAD"), cancellable = true, require = 1)
    private void meldrite$sendSuggestions(RequestCommandCompletionsC2SPacket request, Suggestions suggestions,
            CallbackInfo callback) {
        if (!NativeEntityExtensions.eventsAvailable()) return;
        callback.cancel();
        NativeCommandTreeEvents.sendSuggestions(player, request.getCompletionId(), request.getPartialCommand(), suggestions);
    }
}
