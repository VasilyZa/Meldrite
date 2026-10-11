package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeEntityExtensions;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.bukkit.Location;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class PaperConnectionTeleportMixin {
    public void teleport(Location destination) {
        NativeEntityExtensions.teleport((ServerPlayNetworkHandler) (Object) this, destination);
    }
}
