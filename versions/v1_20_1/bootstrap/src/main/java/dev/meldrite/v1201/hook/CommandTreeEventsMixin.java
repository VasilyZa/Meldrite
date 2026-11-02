package dev.meldrite.v1201.hook;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import dev.meldrite.v1201.compat.NativeCommandTreeEvents;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandManager.class)
public abstract class CommandTreeEventsMixin {
    @Inject(method = "sendCommandTree", at = @At("HEAD"), cancellable = true, require = 1)
    private void meldrite$disabledTree(ServerPlayerEntity player, CallbackInfo callback) {
        if (org.spigotmc.SpigotConfig.tabComplete < 0) {
            player.networkHandler.sendPacket(new CommandTreeS2CPacket(new RootCommandNode<>()));
            callback.cancel();
        }
    }

    @WrapOperation(method = "makeTreeForSource", at = @At(value = "INVOKE",
            target = "Lcom/mojang/brigadier/tree/CommandNode;canUse(Ljava/lang/Object;)Z"), require = 1)
    private boolean meldrite$visibleNode(CommandNode<ServerCommandSource> node, Object source, Operation<Boolean> original) {
        return (org.spigotmc.SpigotConfig.sendNamespaced || !node.getName().contains(":")) && original.call(node, source);
    }

    @Inject(method = "sendCommandTree", at = @At(value = "NEW",
            target = "net/minecraft/network/packet/s2c/play/CommandTreeS2CPacket"), require = 1)
    private void meldrite$prepareClientTree(ServerPlayerEntity player, CallbackInfo callback,
            @Local RootCommandNode<CommandSource> root) {
        NativeCommandTreeEvents.prepare(player, root);
    }
}
