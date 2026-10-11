package dev.meldrite.v1201.hook;

import net.minecraft.server.dedicated.DedicatedPlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.spongepowered.asm.mixin.Mixin;

/** Bukkit respawn bridge to the native 1.20.1 player manager. */
@Mixin(DedicatedPlayerManager.class)
public abstract class LegacyDedicatedPlayerManagerAliasesMixin {
    public ServerPlayerEntity respawn(ServerPlayerEntity player, ServerWorld world, boolean alive,
            Location location, boolean flag, PlayerRespawnEvent.RespawnReason reason) {
        return dev.meldrite.v1201.compat.NativePlayerRespawn.requested(location, reason,
                () -> ((DedicatedPlayerManager) (Object) this).respawnPlayer(player, alive));
    }
}
