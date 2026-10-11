package dev.meldrite.v1201.hook;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;

/** Short MinecraftServer entry points used by older CraftBukkit adapters. */
@Mixin(MinecraftServer.class)
public abstract class LegacyMinecraftServerAliasesMixin {
    public Iterable<ServerWorld> E() {
        return ((MinecraftServer) (Object) this).getWorlds();
    }

    public ServerWorld F() {
        return ((MinecraftServer) (Object) this).getOverworld();
    }

    public ServerAdvancementLoader ax() {
        return ((MinecraftServer) (Object) this).getAdvancementLoader();
    }

    public ServerAdvancementLoader ay() {
        return ((MinecraftServer) (Object) this).getAdvancementLoader();
    }
}
