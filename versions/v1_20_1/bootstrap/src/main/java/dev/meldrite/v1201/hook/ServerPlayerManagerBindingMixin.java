package dev.meldrite.v1201.hook;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Publishes native player-manager setup to the already-created Bukkit server facade. */
@Mixin(MinecraftServer.class)
public abstract class ServerPlayerManagerBindingMixin {
    @Inject(method = "setPlayerManager", at = @At("RETURN"))
    private void meldrite$bindPlayerManager(PlayerManager manager, CallbackInfo callback) {
        if (Bukkit.getServer() instanceof CraftServer server && server.getServer() == (Object) this) {
            server.meldrite$bindPlayerList(manager);
        }
    }
}
